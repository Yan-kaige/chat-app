package com.kai.util;

public class SnowId {
    // 常量部分
    private static final long EPOCH = 1672531200000L; // 自定义起始时间戳 (2023-01-01)
    private static final long MACHINE_ID_BITS = 10; // 机器 ID 占用位数
    private static final long SEQUENCE_BITS = 12; // 序列号占用位数

    private static final long MAX_MACHINE_ID = (1L << MACHINE_ID_BITS) - 1; // 最大机器 ID (1023)
    private static final long MAX_SEQUENCE = (1L << SEQUENCE_BITS) - 1; // 最大序列号 (4095)

    private static final long MACHINE_ID_SHIFT = SEQUENCE_BITS; // 机器 ID 左移位数
    private static final long TIMESTAMP_SHIFT = SEQUENCE_BITS + MACHINE_ID_BITS; // 时间戳左移位数

    // 状态部分 (静态变量)
    private static long machineId; // 当前机器 ID
    private static long sequence = 0; // 当前毫秒内的序列号
    private static long lastTimestamp = -1L; // 上次生成 ID 的时间戳

    // 静态代码块，用于初始化 machineId
    static {
        machineId = 1; // 默认设置为 1，可以根据实际需求调整
        if (machineId < 0 || machineId > MAX_MACHINE_ID) {
            throw new IllegalArgumentException("Machine ID must be between 0 and " + MAX_MACHINE_ID);
        }
    }

    // 生成下一个 ID (静态方法)
    public static synchronized long nextId() {
        long currentTimestamp = currentTimeMillis();

        // 检查系统时钟是否被回拨
        if (currentTimestamp < lastTimestamp) {
            throw new RuntimeException(
                    "Clock moved backwards. Refusing to generate ID for " + (lastTimestamp - currentTimestamp) + " milliseconds"
            );
        }

        // 同一毫秒内生成 ID
        if (currentTimestamp == lastTimestamp) {
            sequence = (sequence + 1) & MAX_SEQUENCE; // 序列号循环
            if (sequence == 0) {
                // 如果序列号用完，等待下一毫秒
                currentTimestamp = waitNextMillis(lastTimestamp);
            }
        } else {
            // 不同毫秒内，重置序列号
            sequence = 0;
        }

        lastTimestamp = currentTimestamp;

        // 生成 Snowflake ID
        return ((currentTimestamp - EPOCH) << TIMESTAMP_SHIFT) // 时间戳部分
                | (machineId << MACHINE_ID_SHIFT)              // 机器 ID 部分
                | sequence;                                    // 序列号部分
    }

    // 等待下一毫秒
    private static long waitNextMillis(long lastTimestamp) {
        long timestamp = currentTimeMillis();
        while (timestamp <= lastTimestamp) {
            timestamp = currentTimeMillis();
        }
        return timestamp;
    }

    // 获取当前时间戳（毫秒）
    private static long currentTimeMillis() {
        return System.currentTimeMillis();
    }

    // 测试方法
    public static void main(String[] args) {
        // 无需调用 init，直接调用 nextId() 方法
            System.out.println(SnowId.nextId());
    }
}
