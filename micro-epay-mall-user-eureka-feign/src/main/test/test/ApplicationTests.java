package test;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ApplicationTests {

	@Value("${filter}")
	private Map<String, String> filterChainDefinitionMap = new LinkedHashMap<String,String>();

	@Test
	public void contextLoads() {
	}

	@Test
	public void testYmlMap() {
		for(String key: filterChainDefinitionMap.keySet()) {
			System.out.println(key + ":" + filterChainDefinitionMap.get(key));
		}
	}


	public Map<String, String> getFilterChainDefinitionMap() {
		return filterChainDefinitionMap;
	}
	public void setFilterChainDefinitionMap(Map<String, String> filterChainDefinitionMap) {
		this.filterChainDefinitionMap = filterChainDefinitionMap;
	}
}
