package org.egov.requestvalidation.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;
import org.junit.jupiter.api.Test;

class EntityAndPercentDecoderTest {
    private final EntityAndPercentDecoder decoder = new EntityAndPercentDecoder();

    // The detector skips a decoder when its marker is absent; that is only sound if the decoder
    // would have returned the value unchanged.
    @Test
    void decodersNeverChangeValuesWithoutTheirMarker() {
        char[] alphabet = "&%#;:<> aZx09fFéन \t".toCharArray();
        Random random = new Random(11);
        int entityCandidates = 0;
        int percentCandidates = 0;
        for (int run = 0; run < 50000; run++) {
            char[] value = new char[random.nextInt(12)];
            for (int index = 0; index < value.length; index++) {
                value[index] = alphabet[random.nextInt(alphabet.length)];
            }
            String text = new String(value);
            if (EntityAndPercentDecoder.mayDecodeEntities(text)) {
                entityCandidates++;
            } else {
                assertEquals(text, decoder.decodeEntities(text), text);
            }
            if (EntityAndPercentDecoder.mayDecodePercent(text)) {
                percentCandidates++;
            } else {
                assertEquals(text, decoder.decodePercent(text), text);
            }
        }
        assertTrue(entityCandidates > 200 && percentCandidates > 200,
                "both branches exercised: entity=" + entityCandidates + " percent=" + percentCandidates);
    }
}
