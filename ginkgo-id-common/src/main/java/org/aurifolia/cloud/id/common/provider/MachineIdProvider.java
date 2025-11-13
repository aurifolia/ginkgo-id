package org.aurifolia.cloud.id.common.provider;

/**
 * 机器ID提供器
 *
 * @author Peng Dan
 * @since 1.0
 */
@FunctionalInterface
public interface MachineIdProvider {
    /**
     * 分配机器ID
     *
     * @return 机器ID
     */
    long allocate();
}
