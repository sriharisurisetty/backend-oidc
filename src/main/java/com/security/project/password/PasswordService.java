package com.security.project.password;

import com.security.project.password.InvalidRequestException;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class PasswordService {

    private static final int MIN_LENGTH = 8;
    private static final int MAX_LENGTH = 128;

    private static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String DIGITS = "0123456789";
    private static final String SPECIAL = "!@#$%^&*-_+=";

    private static final String ALL_ALLOWED = UPPER + LOWER + DIGITS + SPECIAL;

    private final SecureRandom random = new SecureRandom();

    public String generate(int length) {
        if (length < MIN_LENGTH || length > MAX_LENGTH) {
            throw new InvalidRequestException("length must be between " + MIN_LENGTH + " and " + MAX_LENGTH);
        }

        List<Character> chars = new ArrayList<>(length);

        // Ensure required character classes
        chars.add(randomCharFrom(UPPER));
        chars.add(randomCharFrom(LOWER));
        chars.add(randomCharFrom(DIGITS));
        chars.add(randomCharFrom(SPECIAL));

        // Fill the rest
        for (int i = chars.size(); i < length; i++) {
            chars.add(randomCharFrom(ALL_ALLOWED));
        }

        // Shuffle to avoid predictable positions
        Collections.shuffle(chars, random);

        StringBuilder sb = new StringBuilder(length);
        for (char c : chars) sb.append(c);
        return sb.toString();
    }

    private char randomCharFrom(String src) {
        int idx = random.nextInt(src.length());
        return src.charAt(idx);
    }
}