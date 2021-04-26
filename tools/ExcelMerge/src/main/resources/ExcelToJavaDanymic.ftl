package ${packageName};

import com.kwai.clover.core.support.SysException;
import com.kwai.clover.core.configData.DanymicConfBase;



/**
 * @author System
 * 配置表动态类
 */
public class Danymic${entityName} extends DanymicConfBase{
	/** 配置 */
	private ${entityName} conf;
	/** 字段位 */
	private int field_unit_bits;

	<#-- 字段 -->
	<#list danymicFields as prop>
	<#if prop.name == "sn">
	<#else>
	/** ${prop.note} */
	private ${prop.type} ${prop.name};
	</#if>
	</#list>

	/** 构造方法 */
	public Danymic${entityName}(int sn) {
		this.conf = ${entityName}.get(sn);
		if(this.conf == null){
			throw new SysException("${entityName} not exist, sn={}", sn);
		}
	}

	/** 构造方法 */
	public Danymic${entityName}(${entityName} conf) {
		if(conf == null){
			throw new SysException("${entityName} not exist");
		}
		this.conf = conf;
	}

	/** 清理所有动态字段值 */
	public void clearAllField(){
		<#list danymicFields as prop>
		<#if prop.name == "sn">
		<#else>
		this.field_unit_bits |= ~${prop.unitBit};
		</#if>
		</#list>
	}

	<#list danymicFields as prop>
	public void set${prop.name?cap_first}(${prop.type} ${prop.name}){
		this.field_unit_bits |= ${prop.unitBit};
		this.${prop.name} = ${prop.name};
	}

	public ${prop.type} get${prop.name?cap_first}(){
		if((this.field_unit_bits & ${prop.unitBit}) == ${prop.unitBit}){
			return this.${prop.name};
		}
		return conf.${prop.name};
	}

	public void clear${prop.name?cap_first}(){
		this.field_unit_bits |= ~${prop.unitBit};
	}

	public boolean isChange${prop.name?cap_first}(){
		return (this.field_unit_bits & ${prop.unitBit}) == ${prop.unitBit};
	}
	</#list>

	<#list confFields as prop>
	public ${prop.type} get${prop.name?cap_first}(){
		return conf.${prop.name};
	}
	</#list>


	@Override
	@SuppressWarnings("unchecked")
	public <T> T getFieldValue(String key){
	
		Object value = null;
		switch(key){
			<#list confFields as prop>
			case "${prop.name}":
				value = get${prop.name?cap_first}();
				break;
			</#list>
			<#list danymicFields as prop>
			case "${prop.name}":
				value = get${prop.name?cap_first}();
				break;
			</#list>
			default:break;
		}

	return (T)value;
	}

	@Override
	public void setFieldValue(String key, Object value){
	
		switch(key){
			<#list danymicFields as prop>
			case "${prop.name}":
				set${prop.name?cap_first}((${prop.type})value);
				break;
			</#list>
			default:break;
		}

	}

	public Danymic${entityName} copy(){
		Danymic${entityName} copy = new Danymic${entityName}(conf.sn);
		<#list danymicFields as prop>
		if((this.field_unit_bits & ${prop.unitBit}) == ${prop.unitBit}){
			copy.set${prop.name?cap_first}(get${prop.name?cap_first}());
		}
		</#list>

		return copy;
	}

}