package vn.enlearning.backend.avatar;

import java.net.http.HttpClient;
import java.time.Duration;

import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/** Dựng {@link RestClient} cho các dịch vụ ngoài của module avatar: HTTP/1.1, cùng một hạn chờ cho kết nối và đọc. */
final class AvatarHttp {

	private AvatarHttp() {
	}

	static RestClient client(String baseUrl, Duration timeout) {
		HttpClient httpClient = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).connectTimeout(timeout).build();
		JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
		factory.setReadTimeout(timeout);
		return RestClient.builder().baseUrl(baseUrl).requestFactory(factory).build();
	}
}
