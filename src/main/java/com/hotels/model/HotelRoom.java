package com.hotels.model;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;

import java.util.*;

/**
 * 酒店房间数据模型
 */
@SerializableAs("HotelRoom")
public class HotelRoom implements ConfigurationSerializable {

    private String id;                  // 唯一ID
    private String name;                // 房间名称
    private UUID owner;                 // 房主UUID
    private String ownerName;           // 房主名称（显示用）

    // 区域坐标
    private String worldName;
    private double x1, y1, z1;
    private double x2, y2, z2;

    // 入口坐标（玩家传送点）
    private double spawnX, spawnY, spawnZ;
    private float spawnYaw, spawnPitch;

    private double price;               // 入住价格
    private String password;            // 密码（空表示无密码）
    private boolean locked;             // 是否上锁

    private RoomStatus status;          // 房间状态
    private UUID currentGuest;          // 当前入住的客人
    private String currentGuestName;    // 客人名称

    private long createdTime;           // 创建时间
    private long checkinTime;           // 入住时间（客人入住的时间戳）
    private int durationMinutes;        // 使用时长（分钟），0=不限时，-1=使用合集默认
    private List<String> tags;          // 标签列表

    public HotelRoom() {
        this.id = UUID.randomUUID().toString().substring(0, 8);
        this.status = RoomStatus.AVAILABLE;
        this.locked = false;
        this.durationMinutes = -1;
        this.tags = new ArrayList<>();
        this.createdTime = System.currentTimeMillis();
    }

    public enum RoomStatus {
        AVAILABLE,      // 空闲
        OCCUPIED,       // 已入住
        MAINTENANCE     // 维护中
    }

    // ===== 区域检测方法 =====

    /**
     * 判断坐标是否在房间区域内
     */
    public boolean containsLocation(Location loc) {
        if (!loc.getWorld().getName().equals(worldName)) return false;

        double x = loc.getX();
        double y = loc.getY();
        double z = loc.getZ();

        double minX = Math.min(x1, x2);
        double maxX = Math.max(x1, x2);
        double minY = Math.min(y1, y2);
        double maxY = Math.max(y1, y2);
        double minZ = Math.min(z1, z2);
        double maxZ = Math.max(z1, z2);

        return x >= minX && x <= maxX &&
               y >= minY && y <= maxY &&
               z >= minZ && z <= maxZ;
    }

    /**
     * 判断两个区域是否重叠
     */
    public boolean overlaps(double ox1, double oy1, double oz1,
                            double ox2, double oy2, double oz2) {
        double minX = Math.min(x1, x2);
        double maxX = Math.max(x1, x2);
        double minY = Math.min(y1, y2);
        double maxY = Math.max(y1, y2);
        double minZ = Math.min(z1, z2);
        double maxZ = Math.max(z1, z2);

        double ominX = Math.min(ox1, ox2);
        double omaxX = Math.max(ox1, ox2);
        double ominY = Math.min(oy1, oy2);
        double omaxY = Math.max(oy1, oy2);
        double ominZ = Math.min(oz1, oz2);
        double omaxZ = Math.max(oz1, oz2);

        return maxX >= ominX && minX <= omaxX &&
               maxY >= ominY && minY <= omaxY &&
               maxZ >= ominZ && minZ <= omaxZ;
    }

    // ===== 序列化 =====

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", id);
        map.put("name", name);
        map.put("owner", owner.toString());
        map.put("ownerName", ownerName);
        map.put("world", worldName);
        map.put("x1", x1);
        map.put("y1", y1);
        map.put("z1", z1);
        map.put("x2", x2);
        map.put("y2", y2);
        map.put("z2", z2);
        map.put("spawnX", spawnX);
        map.put("spawnY", spawnY);
        map.put("spawnZ", spawnZ);
        map.put("spawnYaw", spawnYaw);
        map.put("spawnPitch", spawnPitch);
        map.put("price", price);
        map.put("password", password != null ? password : "");
        map.put("locked", locked);
        map.put("status", status.name());
        map.put("currentGuest", currentGuest != null ? currentGuest.toString() : "");
        map.put("currentGuestName", currentGuestName != null ? currentGuestName : "");
        map.put("createdTime", createdTime);
        map.put("checkinTime", checkinTime);
        map.put("durationMinutes", durationMinutes);
        map.put("tags", tags);
        return map;
    }

    @SuppressWarnings("unchecked")
    public static HotelRoom deserialize(Map<String, Object> map) {
        HotelRoom room = new HotelRoom();
        room.id = (String) map.get("id");
        room.name = (String) map.get("name");
        room.owner = UUID.fromString((String) map.get("owner"));
        room.ownerName = (String) map.get("ownerName");
        room.worldName = (String) map.get("world");
        room.x1 = ((Number) map.get("x1")).doubleValue();
        room.y1 = ((Number) map.get("y1")).doubleValue();
        room.z1 = ((Number) map.get("z1")).doubleValue();
        room.x2 = ((Number) map.get("x2")).doubleValue();
        room.y2 = ((Number) map.get("y2")).doubleValue();
        room.z2 = ((Number) map.get("z2")).doubleValue();
        room.spawnX = ((Number) map.get("spawnX")).doubleValue();
        room.spawnY = ((Number) map.get("spawnY")).doubleValue();
        room.spawnZ = ((Number) map.get("spawnZ")).doubleValue();
        room.spawnYaw = ((Number) map.get("spawnYaw")).floatValue();
        room.spawnPitch = ((Number) map.get("spawnPitch")).floatValue();
        room.price = ((Number) map.get("price")).doubleValue();
        room.password = (String) map.get("password");
        if (room.password.isEmpty()) room.password = null;
        room.locked = (boolean) map.get("locked");
        room.status = RoomStatus.valueOf((String) map.get("status"));
        String guestStr = (String) map.get("currentGuest");
        room.currentGuest = guestStr.isEmpty() ? null : UUID.fromString(guestStr);
        room.currentGuestName = (String) map.get("currentGuestName");
        if (room.currentGuestName != null && room.currentGuestName.isEmpty()) room.currentGuestName = null;
        room.createdTime = ((Number) map.get("createdTime")).longValue();
        room.checkinTime = ((Number) map.get("checkinTime")).longValue();
        room.durationMinutes = ((Number) map.getOrDefault("durationMinutes", -1)).intValue();
        room.tags = (List<String>) map.getOrDefault("tags", new ArrayList<>());
        return room;
    }

    // ===== Getter / Setter =====

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public UUID getOwner() { return owner; }
    public void setOwner(UUID owner) { this.owner = owner; }

    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }

    public String getWorldName() { return worldName; }
    public void setWorldName(String worldName) { this.worldName = worldName; }

    public double getX1() { return x1; }
    public void setX1(double x1) { this.x1 = x1; }
    public double getY1() { return y1; }
    public void setY1(double y1) { this.y1 = y1; }
    public double getZ1() { return z1; }
    public void setZ1(double z1) { this.z1 = z1; }
    public double getX2() { return x2; }
    public void setX2(double x2) { this.x2 = x2; }
    public double getY2() { return y2; }
    public void setY2(double y2) { this.y2 = y2; }
    public double getZ2() { return z2; }
    public void setZ2(double z2) { this.z2 = z2; }

    public double getSpawnX() { return spawnX; }
    public void setSpawnX(double spawnX) { this.spawnX = spawnX; }
    public double getSpawnY() { return spawnY; }
    public void setSpawnY(double spawnY) { this.spawnY = spawnY; }
    public double getSpawnZ() { return spawnZ; }
    public void setSpawnZ(double spawnZ) { this.spawnZ = spawnZ; }
    public float getSpawnYaw() { return spawnYaw; }
    public void setSpawnYaw(float spawnYaw) { this.spawnYaw = spawnYaw; }
    public float getSpawnPitch() { return spawnPitch; }
    public void setSpawnPitch(float spawnPitch) { this.spawnPitch = spawnPitch; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public boolean hasPassword() { return password != null && !password.isEmpty(); }

    public boolean isLocked() { return locked; }
    public void setLocked(boolean locked) { this.locked = locked; }

    public RoomStatus getStatus() { return status; }
    public void setStatus(RoomStatus status) { this.status = status; }

    public UUID getCurrentGuest() { return currentGuest; }
    public void setCurrentGuest(UUID currentGuest) { this.currentGuest = currentGuest; }

    public String getCurrentGuestName() { return currentGuestName; }
    public void setCurrentGuestName(String currentGuestName) { this.currentGuestName = currentGuestName; }

    public long getCreatedTime() { return createdTime; }
    public void setCreatedTime(long createdTime) { this.createdTime = createdTime; }

    public long getCheckinTime() { return checkinTime; }
    public void setCheckinTime(long checkinTime) { this.checkinTime = checkinTime; }

    public int getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }

    /**
     * 获取实际使用时长（分钟），0=不限时
     * @param collectionDuration 合集默认时长
     */
    public int getEffectiveDurationMinutes(int collectionDuration) {
        if (durationMinutes == -1) return collectionDuration;
        return durationMinutes;
    }

    /**
     * 获取时长显示文本
     */
    public String getDurationDisplay(int collectionDuration) {
        int effective = getEffectiveDurationMinutes(collectionDuration);
        if (effective <= 0) return "§a不限时";
        if (effective < 60) return "§e" + effective + " 分钟";
        int hours = effective / 60;
        int mins = effective % 60;
        if (mins == 0) return "§e" + hours + " 小时";
        return "§e" + hours + " 小时 " + mins + " 分钟";
    }

    public boolean isOccupied() { return status == RoomStatus.OCCUPIED; }
    public boolean isAvailable() { return status == RoomStatus.AVAILABLE; }

    // ===== 标签操作 =====

    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }

    /**
     * 添加标签
     */
    public boolean addTag(String tag) {
        if (tags.size() >= 3) return false;
        if (tags.contains(tag)) return false;
        return tags.add(tag);
    }

    /**
     * 移除标签
     */
    public boolean removeTag(String tag) {
        return tags.remove(tag);
    }

    /**
     * 是否有指定标签
     */
    public boolean hasTag(String tag) {
        return tags.contains(tag);
    }

    /**
     * 获取标签显示文本
     */
    public String getTagsDisplay() {
        if (tags.isEmpty()) return "§7无";
        return "§e" + String.join("§7, §e", tags);
    }

    /**
     * 获取区域体积（方块数）
     */
    public long getVolume() {
        long dx = Math.abs((long) Math.ceil(x1) - (long) Math.ceil(x2)) + 1;
        long dy = Math.abs((long) Math.ceil(y1) - (long) Math.ceil(y2)) + 1;
        long dz = Math.abs((long) Math.ceil(z1) - (long) Math.ceil(z2)) + 1;
        return dx * dy * dz;
    }
}
