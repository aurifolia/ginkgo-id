package org.aurifolia.cloud.id.sdk.internal;

/**
 * 序列号块（可变，线程内复用）
 *
 * @author Peng Dan
 * @since 2.0
 */
final class Chunk {

    long segmentNumber;
    long seqStart;
    long seqEnd;
}
