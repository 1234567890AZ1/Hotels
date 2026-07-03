package com.hotels;

import com.hotels.model.HotelRoom;
import com.hotels.model.RoomCollection;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * HTTP API 服务器 - 提供 RESTful 接口查询房间和合集数据
 */
public class ApiServer {

    private final HotelsPlugin plugin;
    private HttpServer server;
    private int port;

    public ApiServer(HotelsPlugin plugin) {
        this.plugin = plugin;
        this.port = plugin.getConfig().getInt("api-port", 25566);
    }

    /**
     * 启动 API 服务器
     */
    public void start() {
        try {
            server = HttpServer.create(new InetSocketAddress(port), 0);

            // 注册路由
            server.createContext("/", new WebPageHandler());
            server.createContext("/api/rooms", new RoomsHandler());
            server.createContext("/api/collections", new CollectionsHandler());
            server.createContext("/api/checkin", new CheckinHandler());
            server.createContext("/api/checkout", new CheckoutHandler());
            server.createContext("/api/player", new PlayerHandler());

            server.setExecutor(null);
            server.start();
            plugin.getLogger().info("HTTP API 服务器已启动，端口: " + port);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "无法启动 HTTP API 服务器（端口 " + port + " 可能被占用）", e);
        }
    }

    /**
     * 停止 API 服务器
     */
    public void stop() {
        if (server != null) {
            server.stop(0);
            plugin.getLogger().info("HTTP API 服务器已停止");
        }
    }

    // ===== 工具方法 =====

    private void sendJson(HttpExchange exchange, int status, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.sendResponseHeaders(status, bytes.length);

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private void sendError(HttpExchange exchange, int status, String message) throws IOException {
        sendJson(exchange, status, "{\"success\":false,\"error\":\"" + jsonEscape(message) + "\"}");
    }

    /**
     * JSON 字符串转义
     */
    private String jsonEscape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    /**
     * 将 Map 转为 JSON 字符串（简单实现，仅支持 String/Number/Boolean/List/Map）
     */
    @SuppressWarnings("unchecked")
    private String toJson(Object obj) {
        if (obj == null) return "null";
        if (obj instanceof String) return "\"" + jsonEscape((String) obj) + "\"";
        if (obj instanceof Number || obj instanceof Boolean) return obj.toString();
        if (obj instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) obj;
            StringBuilder sb = new StringBuilder("{");
            boolean first = true;
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                if (!first) sb.append(",");
                first = false;
                sb.append("\"").append(jsonEscape(entry.getKey())).append("\":");
                sb.append(toJson(entry.getValue()));
            }
            sb.append("}");
            return sb.toString();
        }
        if (obj instanceof List) {
            List<Object> list = (List<Object>) obj;
            StringBuilder sb = new StringBuilder("[");
            boolean first = true;
            for (Object item : list) {
                if (!first) sb.append(",");
                first = false;
                sb.append(toJson(item));
            }
            sb.append("]");
            return sb.toString();
        }
        return "\"" + jsonEscape(obj.toString()) + "\"";
    }

    /**
     * 简单 JSON 对象解析（仅支持一层 key-value，值只支持 String）
     */
    private Map<String, String> parseSimpleJson(String json) {
        Map<String, String> result = new HashMap<>();
        if (json == null || json.trim().isEmpty()) return result;

        json = json.trim();
        if (!json.startsWith("{") || !json.endsWith("}")) return result;
        json = json.substring(1, json.length() - 1).trim();

        // 按逗号分割，但要注意字符串内的逗号
        boolean inString = false;
        StringBuilder key = new StringBuilder();
        StringBuilder value = new StringBuilder();
        boolean readingKey = true;

        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '"') {
                inString = !inString;
                continue;
            }
            if (inString) {
                if (readingKey) key.append(c);
                else value.append(c);
                continue;
            }
            if (c == ':') {
                readingKey = false;
                continue;
            }
            if (c == ',') {
                if (key.length() > 0) {
                    result.put(key.toString().trim(), value.toString().trim());
                }
                key = new StringBuilder();
                value = new StringBuilder();
                readingKey = true;
                continue;
            }
            if (c == ' ' || c == '\n' || c == '\r' || c == '\t') continue;
            if (readingKey) key.append(c);
            else value.append(c);
        }
        if (key.length() > 0) {
            result.put(key.toString().trim(), value.toString().trim());
        }

        return result;
    }

    /**
     * 读取 web 资源文件
     */
    private String loadWebResource(String path) {
        try {
            java.io.InputStream is = getClass().getClassLoader().getResourceAsStream("web/" + path);
            if (is == null) return null;
            return new String(is.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            plugin.getLogger().warning("无法加载 web 资源: " + path);
            return null;
        }
    }

    /**
     * 网页页面处理器 - 返回管理页面
     */
    private class WebPageHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String requestPath = exchange.getRequestURI().getPath();

            // 根路径返回 index.html
            if (requestPath.equals("/") || requestPath.equals("/index.html")) {
                String html = loadWebResource("index.html");
                if (html == null) {
                    String error = "<html><body><h1>404 - 页面未找到</h1><p>请确认 web/index.html 已打包到插件中</p></body></html>";
                    byte[] bytes = error.getBytes(java.nio.charset.StandardCharsets.UTF_8);
                    exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
                    exchange.sendResponseHeaders(404, bytes.length);
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(bytes);
                    }
                    return;
                }
                byte[] bytes = html.getBytes(java.nio.charset.StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
                exchange.sendResponseHeaders(200, bytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(bytes);
                }
                return;
            }

            // 其他路径返回 404
            sendError(exchange, 404, "Not Found");
        }
    }

    private Map<String, Object> roomToMap(HotelRoom room) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", room.getId());
        map.put("name", room.getName());
        map.put("owner", room.getOwnerName());
        map.put("ownerUUID", room.getOwner().toString());
        map.put("world", room.getWorldName());
        map.put("price", room.getPrice());
        map.put("currentPrice", room.getCurrentPrice());
        map.put("status", room.getStatus().name());
        map.put("locked", room.isLocked());
        map.put("hasPassword", room.hasPassword());
        map.put("tags", room.getTags());
        map.put("volume", room.getVolume());
        map.put("hasDiscount", room.hasActiveDiscount());
        if (room.hasActiveDiscount()) {
            map.put("discountPrice", room.getDiscountPrice());
            map.put("discountExpire", room.getDiscountExpire());
        }
        if (room.isOccupied()) {
            map.put("guest", room.getCurrentGuestName());
        }
        return map;
    }

    private Map<String, Object> collectionToMap(RoomCollection col) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", col.getId());
        map.put("name", col.getName());
        map.put("owner", col.getOwnerName());
        map.put("ownerUUID", col.getOwner().toString());
        map.put("roomCount", col.getRoomCount());
        map.put("adminCount", col.getAdminCount());
        map.put("durationMinutes", col.getDurationMinutes());
        return map;
    }

    private Map<String, String> parseQuery(URI uri) {
        Map<String, String> params = new HashMap<>();
        String query = uri.getQuery();
        if (query == null) return params;
        for (String param : query.split("&")) {
            String[] parts = param.split("=", 2);
            if (parts.length == 2) {
                params.put(parts[0], parts[1]);
            }
        }
        return params;
    }

    // ===== 路由处理器 =====

    /**
     * /api/rooms - 获取房间信息
     * GET /api/rooms?id=xxx - 获取单个房间
     * GET /api/rooms?owner=xxx - 获取玩家所有房间
     * GET /api/rooms?available=true - 获取所有空闲房间
     * GET /api/rooms?tag=xxx - 按标签筛选
     */
    private class RoomsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!exchange.getRequestMethod().equalsIgnoreCase("GET")) {
                sendError(exchange, 405, "仅支持 GET 请求");
                return;
            }

            Map<String, String> params = parseQuery(exchange.getRequestURI());

            try {
                if (params.containsKey("id")) {
                    // 获取单个房间
                    HotelRoom room = plugin.getRoomStorage().getRoom(params.get("id"));
                    if (room == null) {
                        sendError(exchange, 404, "房间不存在");
                        return;
                    }
                    sendJson(exchange, 200, toJson(roomToMap(room)));

                } else if (params.containsKey("owner")) {
                    // 获取玩家所有房间
                    String ownerName = params.get("owner");
                    List<Map<String, Object>> result = new ArrayList<>();
                    for (HotelRoom room : plugin.getRoomStorage().getAllRooms()) {
                        if (room.getOwnerName().equalsIgnoreCase(ownerName)) {
                            result.add(roomToMap(room));
                        }
                    }
                    Map<String, Object> resp = new HashMap<>();
                    resp.put("success", true);
                    resp.put("rooms", result);
                    resp.put("count", result.size());
                    sendJson(exchange, 200, toJson(resp));

                } else if (params.containsKey("available")) {
                    // 获取空闲房间
                    List<Map<String, Object>> result = plugin.getRoomStorage().getAvailableRooms()
                            .stream().map(this::roomToMap).collect(Collectors.toList());

                    if (params.containsKey("tag")) {
                        String tag = params.get("tag");
                        result = result.stream()
                                .filter(r -> ((List<String>) r.get("tags")).contains(tag))
                                .collect(Collectors.toList());
                    }

                    Map<String, Object> resp = new HashMap<>();
                    resp.put("success", true);
                    resp.put("rooms", result);
                    resp.put("count", result.size());
                    sendJson(exchange, 200, toJson(resp));

                } else {
                    // 获取所有房间
                    List<Map<String, Object>> result = plugin.getRoomStorage().getAllRooms()
                            .stream().map(this::roomToMap).collect(Collectors.toList());
                    Map<String, Object> resp = new HashMap<>();
                    resp.put("success", true);
                    resp.put("rooms", result);
                    resp.put("count", result.size());
                    sendJson(exchange, 200, toJson(resp));
                }
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "API 请求处理出错", e);
                sendError(exchange, 500, "服务器内部错误");
            }
        }

        private Map<String, Object> roomToMap(HotelRoom room) {
            return ApiServer.this.roomToMap(room);
        }
    }

    /**
     * /api/collections - 获取合集信息
     * GET /api/collections?id=xxx - 获取单个合集
     * GET /api/collections?owner=xxx - 获取玩家合集
     * GET /api/collections?all=true - 获取所有合集
     */
    private class CollectionsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!exchange.getRequestMethod().equalsIgnoreCase("GET")) {
                sendError(exchange, 405, "仅支持 GET 请求");
                return;
            }

            Map<String, String> params = parseQuery(exchange.getRequestURI());

            try {
                if (params.containsKey("id")) {
                    RoomCollection col = plugin.getRoomStorage().getCollection(params.get("id"));
                    if (col == null) {
                        sendError(exchange, 404, "合集不存在");
                        return;
                    }
                    Map<String, Object> resp = collectionToMap(col);
                    resp.put("success", true);
                    // 附带房间列表
                    List<Map<String, Object>> rooms = plugin.getRoomStorage().getCollectionRooms(col.getId())
                            .stream().map(ApiServer.this::roomToMap).collect(Collectors.toList());
                    resp.put("rooms", rooms);
                    sendJson(exchange, 200, toJson(resp));

                } else if (params.containsKey("owner")) {
                    String ownerName = params.get("owner");
                    List<Map<String, Object>> result = new ArrayList<>();
                    for (RoomCollection col : plugin.getRoomStorage().getAllCollections()) {
                        if (col.getOwnerName().equalsIgnoreCase(ownerName)) {
                            result.add(collectionToMap(col));
                        }
                    }
                    Map<String, Object> resp = new HashMap<>();
                    resp.put("success", true);
                    resp.put("collections", result);
                    resp.put("count", result.size());
                    sendJson(exchange, 200, toJson(resp));

                } else {
                    List<Map<String, Object>> result = plugin.getRoomStorage().getAllCollections()
                            .stream().map(this::collectionToMap).collect(Collectors.toList());
                    Map<String, Object> resp = new HashMap<>();
                    resp.put("success", true);
                    resp.put("collections", result);
                    resp.put("count", result.size());
                    sendJson(exchange, 200, toJson(resp));
                }
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "API 请求处理出错", e);
                sendError(exchange, 500, "服务器内部错误");
            }
        }

        private Map<String, Object> collectionToMap(RoomCollection col) {
            return ApiServer.this.collectionToMap(col);
        }
    }

    /**
     * /api/checkin - 入住房间
     * POST /api/checkin
     * body: {"player":"玩家名","roomId":"房间ID","password":"密码(可选)"}
     */
    private class CheckinHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                sendError(exchange, 405, "仅支持 POST 请求");
                return;
            }

            try {
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                Map<String, String> data = parseSimpleJson(body);

                String playerName = data.get("player");
                String roomId = data.get("roomId");
                String password = data.get("password");

                if (playerName == null || roomId == null) {
                    sendError(exchange, 400, "缺少必要参数: player, roomId");
                    return;
                }

                org.bukkit.OfflinePlayer offlinePlayer = Arrays.stream(plugin.getServer().getOfflinePlayers())
                        .filter(p -> p.getName() != null && p.getName().equalsIgnoreCase(playerName))
                        .findFirst().orElse(null);

                if (offlinePlayer == null || !offlinePlayer.isOnline()) {
                    sendError(exchange, 400, "玩家不在线或不存在");
                    return;
                }

                org.bukkit.entity.Player player = offlinePlayer.getPlayer();

                HotelRoom room = plugin.getRoomStorage().getRoom(roomId);
                if (room == null) {
                    sendError(exchange, 404, "房间不存在");
                    return;
                }

                // 检查密码
                if (room.hasPassword() && !player.hasPermission("hotels.bypass")) {
                    if (password == null || !password.equals(room.getPassword())) {
                        sendError(exchange, 403, "密码错误");
                        return;
                    }
                }

                // 执行入住
                plugin.getCheckinHandler().attemptCheckin(player, room);

                Map<String, Object> resp = new HashMap<>();
                resp.put("success", true);
                resp.put("message", "入住成功");
                resp.put("room", roomToMap(room));
                sendJson(exchange, 200, toJson(resp));

            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "API 入住处理出错", e);
                sendError(exchange, 500, "入住失败: " + e.getMessage());
            }
        }
    }

    /**
     * /api/checkout - 退房
     * POST /api/checkout
     * body: {"player":"玩家名"}
     */
    private class CheckoutHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                sendError(exchange, 405, "仅支持 POST 请求");
                return;
            }

            try {
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                Map<String, String> data = parseSimpleJson(body);

                String playerName = data.get("player");
                if (playerName == null) {
                    sendError(exchange, 400, "缺少必要参数: player");
                    return;
                }

                org.bukkit.OfflinePlayer offlinePlayer = Arrays.stream(plugin.getServer().getOfflinePlayers())
                        .filter(p -> p.getName() != null && p.getName().equalsIgnoreCase(playerName))
                        .findFirst().orElse(null);

                if (offlinePlayer == null || !offlinePlayer.isOnline()) {
                    sendError(exchange, 400, "玩家不在线或不存在");
                    return;
                }

                org.bukkit.entity.Player player = offlinePlayer.getPlayer();
                plugin.getCheckinHandler().checkout(player);

                Map<String, Object> resp = new HashMap<>();
                resp.put("success", true);
                resp.put("message", "退房成功");
                sendJson(exchange, 200, toJson(resp));

            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "API 退房处理出错", e);
                sendError(exchange, 500, "退房失败: " + e.getMessage());
            }
        }
    }

    /**
     * /api/player - 获取玩家信息
     * GET /api/player?name=xxx - 获取玩家的房间和合集
     */
    private class PlayerHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!exchange.getRequestMethod().equalsIgnoreCase("GET")) {
                sendError(exchange, 405, "仅支持 GET 请求");
                return;
            }

            Map<String, String> params = parseQuery(exchange.getRequestURI());

            try {
                String playerName = params.get("name");
                if (playerName == null) {
                    sendError(exchange, 400, "缺少参数: name");
                    return;
                }

                // 查找玩家 UUID
                UUID playerUUID = null;
                for (HotelRoom room : plugin.getRoomStorage().getAllRooms()) {
                    if (room.getOwnerName().equalsIgnoreCase(playerName)) {
                        playerUUID = room.getOwner();
                        break;
                    }
                }
                for (RoomCollection col : plugin.getRoomStorage().getAllCollections()) {
                    if (col.getOwnerName().equalsIgnoreCase(playerName)) {
                        playerUUID = col.getOwner();
                        break;
                    }
                }

                if (playerUUID == null) {
                    sendError(exchange, 404, "未找到该玩家的数据");
                    return;
                }

                List<Map<String, Object>> rooms = plugin.getRoomStorage().getRoomsByOwner(playerUUID)
                        .stream().map(ApiServer.this::roomToMap).collect(Collectors.toList());

                List<Map<String, Object>> collections = plugin.getRoomStorage().getCollectionsByOwner(playerUUID)
                        .stream().map(ApiServer.this::collectionToMap).collect(Collectors.toList());

                Map<String, Object> resp = new HashMap<>();
                resp.put("success", true);
                resp.put("player", playerName);
                resp.put("rooms", rooms);
                resp.put("roomCount", rooms.size());
                resp.put("collections", collections);
                resp.put("collectionCount", collections.size());
                sendJson(exchange, 200, toJson(resp));

            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "API 请求处理出错", e);
                sendError(exchange, 500, "服务器内部错误");
            }
        }
    }
}
