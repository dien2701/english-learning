package vn.enlearning.backend.audio;

import java.net.http.HttpClient;
import java.time.Duration;

import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/** Dựng {@link RestClient} cho các dịch vụ ngoài của module audio: HTTP/1.1, cùng một hạn chờ cho kết nối và đọc. */
final class AudioHttp {

	private AudioHttp() {
	}

	static RestClient client(String baseUrl, Duration timeout) {
		HttpClient httpClient = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).connectTimeout(timeout).build();
		JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
		factory.setReadTimeout(timeout);
		return RestClient.builder().baseUrl(baseUrl).requestFactory(factory).build();
	}
}
