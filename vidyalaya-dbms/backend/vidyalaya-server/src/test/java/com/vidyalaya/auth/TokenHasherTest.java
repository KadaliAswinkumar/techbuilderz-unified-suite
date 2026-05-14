package com.vidyalaya.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class TokenHasherTest {

    @Test
    void sha256HexLength() {
        assertEquals(64, TokenHasher.sha256("refresh-token").length());
    }
}
