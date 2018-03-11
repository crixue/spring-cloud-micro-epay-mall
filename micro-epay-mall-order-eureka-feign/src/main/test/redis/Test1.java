package redis;

import java.util.Random;

import org.junit.Test;

public class Test1 {

	@Test
	public void test1() {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < 6; i++) {
			sb.append(new Random().nextInt(10));
		}
		System.out.println(sb.toString());
	}
}
