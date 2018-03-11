import org.apache.shiro.util.RegExPatternMatcher;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

public class Test {

	private static PathMatcher pathMatcher = new AntPathMatcher();
	
	public static void main(String[] args) {
		RegExPatternMatcher regExPatternMatcher = new RegExPatternMatcher();
		String pattern = "/**/manage/**";
		String source = "/product/epay-mall/product/manage/getInfo.do";
		boolean falg = pathMatcher.match(pattern, source);
		System.out.println(falg);
	}

}
