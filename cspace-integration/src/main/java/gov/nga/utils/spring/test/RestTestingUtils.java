package gov.nga.utils.spring.test;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.client.TestRestTemplate.HttpClientOption;
import org.springframework.http.ResponseEntity;

public class RestTestingUtils {
	
	private static final Logger log = LoggerFactory.getLogger(RestTestingUtils.class);

	private static TestRestTemplate restClient = new TestRestTemplate(HttpClientOption.ENABLE_REDIRECTS);

	public static ResponseEntity<String> get(String url, int code, String... contentToValidate) {
		return get(url, code, Arrays.asList(contentToValidate));
	}
	
	public static ResponseEntity<String> get(String url, int code, List<String> contentToValidate) {
		ResponseEntity<String> resp = restClient.getForEntity(url, String.class);
		assertTrue(url + " did not have code " + code, responseCodeValidates(resp, code));
		if ( contentToValidate != null )
			assertTrue(url + " did not pass validation " + contentToValidate, contentValidates(resp, contentToValidate));
		return resp;
	}

	public static boolean responseCodeValidates(ResponseEntity<String> resp, String code) {
		return String.valueOf(resp.getStatusCodeValue()).equals(code);
	}
	
	public static boolean responseCodeValidates(ResponseEntity<String> resp, Integer code) {
		String checkCode = code.toString();
		return responseCodeValidates(resp, checkCode);
	}

	public static boolean contentValidates(ResponseEntity<String> resp, List<String> validation) {
		boolean valid = true;
		// check the body and headers for all of the expected 
		for (String v : validation) {

			boolean found = false;
			boolean finding = true;

			if (v.substring(0,1).equals("!")) {
				v = v.substring(1,v.length());
				finding = false;
			}
			
			String body = resp.getBody();
			if (body != null && body.contains(v)) {
				found = true;
			}

			for (Entry<String,List<String>> e : resp.getHeaders().entrySet()) {
				String k = e.getKey();
				for (String s : e.getValue()) {
					if ( (k != null && k.contains(v)) || (s != null && s.contains(v)) ) {
						found = true;
					}
				}
			}
			
			boolean ok = (finding && found) || (!finding && !found);
			String status = ok ? "Successfully" : "Unsuccessfully";
			String presence = finding ? "presence" : "non-presence";
			log.info(status + " validated " + presence + " of content: " + v);
			valid = valid && ok;
			
		}
		return valid;
	}
}
