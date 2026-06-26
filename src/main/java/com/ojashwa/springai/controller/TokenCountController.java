package com.ojashwa.springai.controller;

import com.knuddels.jtokkit.Encodings;
import com.knuddels.jtokkit.api.Encoding;
import com.knuddels.jtokkit.api.EncodingType;
import com.knuddels.jtokkit.api.IntArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class TokenCountController {
    private final Logger logger = LoggerFactory.getLogger(TokenCountController.class);
    private final Encoding encoding;

    public TokenCountController() {
        this.encoding = Encodings.newDefaultEncodingRegistry()
                .getEncoding(EncodingType.O200K_BASE);
    }

    public record TokenDetail(String token, Integer tokenId) { }

    /**
     * This endpoint takes a message and returns a list of TokenDetail objects which contain the token and its id.
     *
     * @param message the message to encode
     * @return a ResponseEntity containing a list of TokenDetail objects
     */

    @GetMapping("/chat/tokens")
    public ResponseEntity<List<TokenDetail>> estimateTokenCounts(@RequestParam("message") String message) {
        IntArrayList encodedIds = encoding.encode(message);
        List<Integer> ids = encodedIds.boxed();

        logger.info("Encoded tokens: {}", ids);

        List<TokenDetail> tokenDetails = ids.stream().map(id -> {
            IntArrayList singleTokenList = new IntArrayList();
            singleTokenList.add(id);
            return new TokenDetail(encoding.decode(singleTokenList), id);
        }).collect(Collectors.toList());
        return ResponseEntity.ok(tokenDetails);
    }
}
