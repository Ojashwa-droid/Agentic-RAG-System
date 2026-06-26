package com.ojashwa.springai.controller;

import com.ojashwa.springai.model.CountryCities;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.converter.ListOutputConverter;
import org.springframework.ai.converter.MapOutputConverter;
import org.springframework.ai.template.st.StTemplateRenderer;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

/**
 * This REST controller provides endpoints for retrieving structured data from the AI chat client.
 * The controller provides methods for retrieving data in the form of JSON objects, lists, and maps.
 *
 * The endpoints are as follows:
 *
 * <ul>
 * <li>GET /api/chat-bean-list - returns a JSON object with a list of objects based on the user's query.</li>
 * <li>GET /api/chat-list - returns a JSON object with a list of strings based on the user's query.</li>
 * <li>GET /api/chat-map - returns a JSON object with a map of strings based on the user's query.</li>
 * </ul>
 */

@RestController
@RequestMapping("/api")
public class StructuredOutputController {

    private final ChatClient chatClient;

    public StructuredOutputController(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder
                .defaultAdvisors(new SimpleLoggerAdvisor())
                .build();
    }

    /**
     * Returns a JSON object with the cities and country name based on the user's query.
     * The JSON object is in the following format:
     * {
     *   "cities": ["City1", "City2", "City3", "City4", ...],
     *   "country": "CountryName"
     * }
     *
     * @param message the user's query
     * @return a ResponseEntity containing the JSON object
     * @throws Exception if the AI is unable to follow the instruction
     **/
    @GetMapping("/chat-bean")
    public ResponseEntity<CountryCities> chatBean(@RequestParam("message") String message) {
        String systemPrompt = """
                You are a helpful assistant that provides information about cities in a country.
                Respond ONLY with a valid JSON object in the following format:
                {
                  "cities": ["City1", "City2", "City3", "City4", ...],
                  "country": "CountryName"
                }
                Important:
                - Only include the JSON object in your response, no other text
                - Include as many major cities as relevant for the country (not limited to 3)
                - Make sure all city names are properly quoted
                - Ensure the JSON is valid before sending
                - Do not include any markdown formatting or code blocks
                - Provide comprehensive list of major cities for the requested country
                """;

        CountryCities countryCities = chatClient
                .prompt()
                .user(message)
                .system(systemPrompt)
                .call().entity(new BeanOutputConverter<>(CountryCities.class));
//                .entity(CountryCities.class);
        return new ResponseEntity<>(countryCities, HttpStatus.OK);
    }

    /**
     * This API endpoint returns a JSON object containing information about a country based on the user's query.
     * The JSON object is in the following format:
     * {
     *   "cities": ["City1", "City2", "City3", "City4", ...],
     *   "country": "CountryName"
     * }
     *
     * @param message the user's query
     * @return a ResponseEntity containing the JSON object
     * @throws Exception if the AI is unable to follow the instruction
     * @apiNote The user's query should be a country name. The AI will respond with a JSON object containing the country name and a list of its major cities.
     * @apiNote The JSON object is in the following format:
     * {
     *   "cities": ["City1", "City2", "City3", "City4", ...],
     *   "country": "CountryName"
     * }
     * @apiNote The AI will respond with a list of major cities for the requested country. The list will contain at least 3 cities.
     */

    @GetMapping("/chat-list")
    public ResponseEntity<List<String>> chatList(@RequestParam(value = "message", required = false) String message) {

        // Method one of utilizing the ListOutputConverter implementation of the StructuredOutputConverter.
        // In this case, we did not make use of entity() method invocation, rather did everything on our own and
        // left the heavy-lifting of conversion to the converter.

        /*
        Pros:

        Full control over the prompt structure
        Educational - shows how the converter works internally
        Customizable template

        Cons:

        More verbose and error-prone
        Manual null handling required
        Reinvents functionality already provided by Spring AI
        */
        ListOutputConverter listOutputConverter = new ListOutputConverter(new DefaultConversionService());
        String format = listOutputConverter.getFormat();
        String template = """
                Provide me a List of {subject}
                {format}
                """;
        PromptTemplate promptTemplate = PromptTemplate.builder().template(template).variables(
                Map.of("subject", message, "format", format)
        ).build();
        Prompt prompt = promptTemplate.create();

        ChatClient.CallResponseSpec responseSpec = chatClient.prompt(prompt).call();
        Generation generation = responseSpec.chatResponse().getResult();

        List<String> stringList = null;
        if (generation.getOutput().getText() != null) {
            stringList = listOutputConverter.convert(generation.getOutput().getText());
        }
        return new ResponseEntity<>(stringList, HttpStatus.OK);

// Method two of utilizing the ListOutputConverter implementation of the StructuredOutputConverter.

//        List<String> countryCities = chatClient
//                .prompt()
//                .user(message)
//                .call().entity(new ListOutputConverter());
//        return new ResponseEntity<>(countryCities, HttpStatus.OK);
    }

    /**
     * API to get a list of cities for a given country.
     *
     * The API takes a country name as a parameter and returns a list of cities in that country.
     * The list is returned as a JSON object where each city name is a key, and the value contains basic details about that city.
     *
     * @param message the name of the country
     * @return a JSON object containing the list of cities
     */

    @GetMapping("/chat-map")
    public ResponseEntity<Map<String, Object>> chatMap(@RequestParam("message") String message) {
        String userPromptTemplate = """
                Provide information about cities in the country mentioned in the query: <message>
                
                Return a JSON object where each city name is a key, and the value contains basic details about that city.
                The structure should be:
                {
                  "CityName1": {
                    "population": "approximate population",
                    "famous_for": "what the city is known for",
                    "region": "region/state where located"
                  },
                  "CityName2": {
                    "population": "approximate population",
                    "famous_for": "what the city is known for",
                    "region": "region/state where located"
                  }
                }
                
                Important:
                - Include 5-8 major cities for the country
                - Use city names as exact keys (no modifications)
                - Provide accurate basic details for each city
                - Return only valid JSON, no additional text
                - Ensure all property values are strings
                """;

        Map<String, Object> countryCities = chatClient
                .prompt()
                .user(promptUserSpec -> promptUserSpec.text(userPromptTemplate)
                        .param("message", message))
                .templateRenderer(StTemplateRenderer.builder().startDelimiterToken('<').endDelimiterToken('>').build())
                .call().entity(new MapOutputConverter());
        return new ResponseEntity<>(countryCities, HttpStatus.OK);
    }

    /**
     * API to get a list of cities for a given country.
     *
     * The API takes a country name as a parameter and returns a list of cities in that country.
     * The list is returned as a JSON object where each city name is a key, and the value contains basic details about that city.
     *
     * @param message the name of the country
     * @return a JSON object containing the list of cities
     * @apiNote The API takes a country name as a parameter and returns a list of cities in that country.
     * The list is returned as a JSON object where each city name is a key, and the value contains basic details about that city.
     */

    @GetMapping("/chat-bean-list")
    public ResponseEntity<List<CountryCities>> chatBeanList(@RequestParam("message") String message) {

        String systemPrompt = """
                You are a helpful assistant that provides information about multiple countries and their major cities.
                Respond ONLY with a valid JSON array in the following format:
                [
                  {
                    "cities": ["City1", "City2", "City3", ...],
                    "country": "CountryName1"
                  },
                  {
                    "cities": ["City1", "City2", "City3", ...],
                    "country": "CountryName2"
                  }
                ]
                
                Important:
                - Only include the JSON array in your response, no other text
                - For each country, include 5-8 major cities
                - Include multiple countries relevant to the query
                - Make sure all city and country names are properly quoted
                - Ensure the JSON is valid before sending
                - Do not include any markdown formatting or code blocks
                - Provide comprehensive lists of major cities for each country
                - If the query asks for a specific country, still return it as an array with one object
                """;

        List<CountryCities> countryCities = chatClient
                .prompt()
                .user(message)
                .system(systemPrompt)
                .call().entity(new ParameterizedTypeReference<List<CountryCities>>() {
                    @Override
                    public Type getType() {
                        return super.getType();
                    }
                });
        return new ResponseEntity<>(countryCities, HttpStatus.OK);
    }

}