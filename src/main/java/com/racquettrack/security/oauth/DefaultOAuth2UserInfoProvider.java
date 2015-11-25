package com.racquettrack.security.oauth;

import java.io.IOException;
import java.util.Map;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.client.ClientResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.core.Authentication;
import org.springframework.util.Assert;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * A default implementation of {@link OAuth2UserInfoProvider} that obtains a {@link Map} of properties
 * from the OAuth endpoint as specified in {@link OAuth2ServiceProperties}. The OAuth endpoint is expected to
 * return JSON data.
 *
 * @author paul.wheeler
 */
public class DefaultOAuth2UserInfoProvider implements OAuth2UserInfoProvider, InitializingBean {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultOAuth2UserInfoProvider.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private Client client =  null;
    private OAuth2ServiceProperties oAuth2ServiceProperties;

    @Override
    public Map<String, Object> getUserInfoFromProvider(Authentication token) {
        Map<String,Object> userInfo = null;

        try {
            Response clientResponse = getClientResponseFromProviderUsing(token);

            String output = getStringRepresentationFrom(clientResponse);
            LOGGER.debug("Output is {}", output);

            if (isOkay(clientResponse)) {
                userInfo = getUserInfoMapFrom(output);
            } else {
                LOGGER.error("Got error response (code={}) from Provider: {}", clientResponse.getStatus(), output);
            }

        } catch (RuntimeException e) {
            LOGGER.error("Jersey client threw a runtime exception", e);
        }

        return userInfo;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(oAuth2ServiceProperties, "An oAuth2ServiceProperties must be set");
    }

    /**
     * Calls the external OAuth provider and returns the Jersey {@link ClientResponse} object.
     * @param token The {@link Authentication} token to use in the call.
     * @return The {@link ClientResponse} object.
     */
    private Response getClientResponseFromProviderUsing(Authentication token) {
        Client client = getClient();

        WebTarget webTarget = client
                .target(oAuth2ServiceProperties.getUserInfoUri())
                .queryParam(oAuth2ServiceProperties.getAccessTokenName(), (String)token.getCredentials());

        if (oAuth2ServiceProperties.getAdditionalInfoParams() != null) {
            for (Map.Entry<String, String> entry : oAuth2ServiceProperties.getAdditionalInfoParams().entrySet()) {
                webTarget = webTarget.queryParam(entry.getKey(), entry.getValue());
            }
        }

        Response clientResponse = webTarget.request(MediaType.APPLICATION_JSON_TYPE)
                .get();

        return clientResponse;
    }

    private String getStringRepresentationFrom(Response clientResponse) {
        return clientResponse.readEntity(String.class).toString();
    }

    private Map<String, Object> getUserInfoMapFrom(String string) {
        Map<String, Object> userInfo = null;

        try {
            TypeReference typeReference = new TypeReference<Map<String,Object>>(){};
            userInfo = OBJECT_MAPPER.readValue(string, typeReference);
        } catch (IOException e) {
            LOGGER.error("Error getting user info from Provider", e);
        }

        return userInfo;
    }

    private boolean isOkay(Response clientResponse) {
        return clientResponse != null && clientResponse.getStatus() == 200;
    }

    /**
     * For caching the {@link Client} object.
     * @return The Jersey {@link Client} object to use.
     */
    protected Client getClient() {
        if (client == null) {
            client = ClientBuilder.newClient();
        }
        return client;
    }

    /**
     * Intended to be used for unit testing only.
     * @param client The {@link Client} to use. For unit tests allows the client to be mocked.
     */
    void setClient(Client client) {
        this.client = client;
    }

    public void setoAuth2ServiceProperties(OAuth2ServiceProperties oAuth2ServiceProperties) {
        this.oAuth2ServiceProperties = oAuth2ServiceProperties;
    }
}
