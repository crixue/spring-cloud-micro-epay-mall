package redis;

import java.io.Serializable;
import java.util.Map;

import org.springframework.beans.BeanUtils;

import com.google.common.base.Splitter;

public class User implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	private int id;
    private String name;
    private String add;
    private String old;
    
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getAdd() {
		return add;
	}
	public void setAdd(String add) {
		this.add = add;
	}
	public String getOld() {
		return old;
	}
	public void setOld(String old) {
		this.old = old;
	}
    
    

	public static void main(String[] args) {
//		BeanUtils.copyProperties(source, target);

	}
}
