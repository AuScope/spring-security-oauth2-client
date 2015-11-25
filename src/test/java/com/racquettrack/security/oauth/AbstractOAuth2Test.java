package com.racquettrack.security.oauth;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.client.ClientResponse;
import org.mockito.Matchers;

/**
 * Helper class for initialising mocks for OAuth testing.
 *
 * @author paul.wheeler
 */
public class AbstractOAuth2Test {
    protected Client client = mock(Client.class);
    protected WebTarget webTarget = mock(WebTarget.class);
    protected ClientResponse clientResponse = mock(ClientResponse.class);
    protected Response response = mock(Response.class);
    protected Builder builder = mock(Builder.class);

    /**
     * Initialise all the mocks necessary for mocking calls to the OAuth Provider.
     *
     * Allows subclasses to override behaviour. To run tests with different response values, individual tests
     * can always call Mockito again to override the data that will be returned.
     *
     * @param resourceUri  The resource URI that will be used in the call to  {@link Client#resource(String)}.
     * @param defaultResponse The defaultResponse data that will be returned by the call to {@link ClientResponse#getEntity(Class)}.
     */
    protected void initMocks(String resourceUri, String defaultResponse) {
        given(client.target(resourceUri)).willReturn(webTarget);
        given(webTarget.queryParam(Matchers.anyString(), Matchers.anyString())).willReturn(webTarget);
        given(webTarget.request(MediaType.APPLICATION_JSON_TYPE)).willReturn(builder);
        given(builder.post(Matchers.<Entity<?>>any())).willReturn(response);
        given(builder.get(ClientResponse.class)).willReturn(clientResponse);
        given(clientResponse.getStatus()).willReturn(200);
        given(clientResponse.getStatusInfo()).willReturn(Response.Status.OK);
        given(clientResponse.readEntity(String.class)).willReturn(defaultResponse);
        given(response.getStatus()).willReturn(200);
        given(response.getStatusInfo()).willReturn(Response.Status.OK);
        given(response.readEntity(String.class)).willReturn(defaultResponse);
    }
}
