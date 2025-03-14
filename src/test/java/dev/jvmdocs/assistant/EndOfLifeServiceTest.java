package dev.jvmdocs.assistant;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.time.LocalDate;
import java.util.List;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class EndOfLifeServiceTest {

    private final RestClient.RequestHeadersUriSpec requestHeadersUriSpec = mock(RestClient.RequestHeadersUriSpec.class);
    private final RestClient.RequestHeadersSpec requestHeadersSpec = mock(RestClient.RequestHeadersSpec.class);
    private final RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);
    private final RestClient restClient = mock(RestClient.class);
    private final EndOfLifeService service = new EndOfLifeService(restClient);

    @Test
    void shouldFetchSpringBootEolInfo() {
        // Given
        EndOfLifeService.Request request = new EndOfLifeService.Request("spring-boot");
        EndOfLifeService.EolInfo mockEolInfo = new EndOfLifeService.EolInfo(
            "3.0",
            "17",
            LocalDate.of(2022, 11, 24),
            "2024-11-24",
            "3.0.7",
            LocalDate.of(2023, 5, 18),
            true,
            "2024-11-24",
            null
        );
        List<EndOfLifeService.EolInfo> mockEolInfos = List.of(mockEolInfo);

        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(ArgumentMatchers.anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.accept(MediaType.APPLICATION_JSON)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(any(ParameterizedTypeReference.class))).thenReturn(mockEolInfos);

        // When
        EndOfLifeService.Response response = service.apply(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.eolInfos())
            .isNotNull()
            .hasSize(1)
            .first()
            .satisfies(eolInfo -> {
                assertThat(eolInfo.cycle()).isEqualTo("3.0");
                assertThat(eolInfo.release()).isEqualTo("17");
                assertThat(eolInfo.lts()).isTrue();
            });

        verify(restClient).get();
        verify(requestHeadersUriSpec).uri("https://endoflife.date/api/spring-boot.json");
        verify(requestHeadersSpec).accept(MediaType.APPLICATION_JSON);
    }

    @Test
    void shouldHandleProductNameWithSpaces() {
        // Given
        EndOfLifeService.Request request = new EndOfLifeService.Request("visual studio");
        EndOfLifeService.EolInfo mockEolInfo = new EndOfLifeService.EolInfo(
            "2022",
            null,
            LocalDate.of(2022, 1, 1),
            "2032-01-01",
            "17.6",
            LocalDate.of(2023, 1, 1),
            true,
            "2032-01-01",
            null
        );
        List<EndOfLifeService.EolInfo> mockEolInfos = List.of(mockEolInfo);

        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(ArgumentMatchers.anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.accept(MediaType.APPLICATION_JSON)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(any(ParameterizedTypeReference.class))).thenReturn(mockEolInfos);

        // When
        EndOfLifeService.Response response = service.apply(request);

        // Then
        assertThat(response).isNotNull();
        verify(requestHeadersUriSpec).uri("https://endoflife.date/api/visual-studio.json");
    }

    @Test
    void shouldHandleEmptyResponse() {
        // Given
        EndOfLifeService.Request request = new EndOfLifeService.Request("nonexistent-product");
        List<EndOfLifeService.EolInfo> emptyResponse = Collections.emptyList();

        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(ArgumentMatchers.anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.accept(MediaType.APPLICATION_JSON)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(any(ParameterizedTypeReference.class))).thenReturn(emptyResponse);

        // When
        EndOfLifeService.Response response = service.apply(request);

        // Then
        assertThat(response.eolInfos()).isEmpty();
    }

    @Test
    void shouldHandleServerError() {
        // Given
        EndOfLifeService.Request request = new EndOfLifeService.Request("spring-boot");

        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(ArgumentMatchers.anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.accept(MediaType.APPLICATION_JSON)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenThrow(HttpServerErrorException.class);

        // Then
        assertThatThrownBy(() -> service.apply(request))
            .isInstanceOf(HttpServerErrorException.class);
    }

    @Test
    void shouldHandleNetworkError() {
        // Given
        EndOfLifeService.Request request = new EndOfLifeService.Request("spring-boot");

        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(ArgumentMatchers.anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.accept(MediaType.APPLICATION_JSON)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenThrow(ResourceAccessException.class);

        // Then
        assertThatThrownBy(() -> service.apply(request))
            .isInstanceOf(ResourceAccessException.class);
    }
}
