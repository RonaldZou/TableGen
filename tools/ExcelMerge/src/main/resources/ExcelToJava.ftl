package ${packageName};

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.kwai.clover.core.configData.ConfBase;
import com.kwai.clover.core.configData.ConfigData;
import com.kwai.clover.core.interfaces.IReloadSupport;
import com.alibaba.fastjson.JSONObject;
import com.kwai.clover.core.support.log.LogCore;
import com.alibaba.fastjson.JSONArray;

<#if indexs?size != 0>
import java.util.ArrayList;
</#if>

/**
 * ${excelName}
 * @author System
 * 此类是系统自动生成类 不要直接修改，修改后也会被覆盖
 */
@ConfigData
public class ${entityName} extends ConfBase {
	<#-- 字段 -->
	<#list properties as prop>
	/** ${prop.note} */
	public final ${prop.type} ${prop.name};
	</#list>

	<#-- 构造方法 -->
	private ${entityName}(JSONObject data) {
	<#list params as pa>
		this.${pa.name} = ${pa.value};
	</#list>
	}

	private void reset(JSONObject data) {
        Map<String, Field> fieldMap = tempClassField.get(this.getClass());

		<#list params as pa>
		reflectSet(fieldMap.get("${pa.name}"), ${pa.value});
		</#list>
	}

	private static IReloadSupport support = null;
	
	public static void initReloadSupport(IReloadSupport s) {
		support = s;
	}
	
	public static void reload() {
		if (support != null)
			support.beforeReload();
		DATA._init();
		
		if (support != null)
			support.afterReload();
	}

	/**
	 * 获取全部数据
	 * @return
	 */
	public static Collection<${entityName}> findAll() {
		return DATA.getList();
	}

	/**
	 * 通过SN获取数据
	 * @param sn
	 * @return
	 */
	public static ${entityName} get(${idType} sn) {
		return DATA.getMap().get(sn);
	}

		/**
	 * 通过SN获取数据,如果为null，打印log
	 * @param sn
	 * @return
	 */
	public static ${entityName} getNotNull(${idType} sn) {
		${entityName} conf = DATA.getMap().get(sn);
		if(conf == null){
			LogCore.config.error("${entityName} not exit, sn={}", sn);
		}
		return conf;
	}

	/**
	 * 通过SN判断是否拥有该物品（针对装备移除item表的暂时策略）
	 * @param sn
	 * @return
	 */
	public static boolean contains(${idType} sn) {
		return DATA.getMap().containsKey(sn);
	}

	/**
	 * 获取全部key
	 * @return
	 */
	public static Set<${idType}> findKeys() {
		return DATA.getMap().keySet();
	}

	/**
	 * 通过属性获取单条数据
	 * @param params
	 * @return
	 */
	public static ${entityName} getBy(Object...params) {
		List<${entityName}> list = utilBase(DATA.getList(), params);
		if (list.isEmpty()) {
			return null;
		} else {
			return list.get(0);
		}
	}
	
	/**
	 * 通过属性获取数据集合
	 * @param params
	 * @return
	 */
	public static List<${entityName}> findBy(Object...params) {
		return utilBase(DATA.getList(), params);
	}
	<#list indexs as index>
	public static List<${entityName}> findBy${index.name}(${index.paramsWithType}) {
		String key = makeGroupKey(${index.params});
		List<${entityName}> results = DATA.get${index.name}().get(key);
		if (results == null) {
			return new ArrayList<>();
		} else {
			return Collections.unmodifiableList(results);
		}
	}

	public static ${entityName} getBy${index.name}(${index.paramsWithType}) {
		String key = makeGroupKey(${index.params});
		List<${entityName}> results = DATA.get${index.name}().get(key);
		if (results == null || results.isEmpty()) {
			return null;
		} else {
			return results.get(0);
		}
	}
	</#list>

	/**
	 * 属性关键字
	 */
	public static final class K {
		<#list properties as prop>
		/** ${prop.kNote} */
		public static final String ${prop.name} = "${prop.name}";
		</#list>
	}

	/**
	 * 数据集
	 * 单独提出来也是为了做数据延迟初始化
	 * 避免启动遍历类时，触发了static静态块
	 */
	private static final class DATA {
		/** 全部数据 */
		private static volatile Map<${idType}, ${entityName}> _map;
		<#list indexs as index>
		/** index ${index.name} */
		private static volatile Map<String, List<${entityName}>> _map${index.name};
		</#list>

		private static String name = "${csvName}";

		/**
		 * 获取数据的值集合
		 * @return
		 */
		public static Collection<${entityName}> getList() {
			return getMap().values();
		}
		
		/**
		 * 获取Map类型数据集合
		 * @return
		 */
		public static Map<${idType}, ${entityName}> getMap() {
			// 延迟初始化
			if (_map == null) {
				synchronized (DATA.class) {
					if (_map == null) {
						_init();
					}
				}
			}
			
			return _map;
		}
		<#list indexs as index>

		static Map<String, List<${entityName}>> get${index.name}() {
			getMap();
			return _map${index.name};
		}
		</#list>

		/**
		 * 初始化数据
		 */
		private static void _init() {
			Map<${idType}, ${entityName}> dataMap = new HashMap<>();
			<#list indexs as index>
			_map${index.name} = new HashMap<>();
			</#list>
			JSONArray confs = readConfFile(name);
			// 填充实体数据
			for (int i = 0 ; i < confs.size() ; i++) {
				JSONObject conf = confs.getJSONObject(i);
				${idType} sn = (${idType})conf.get("sn");
				${entityName} config = _map == null ? null : _map.get(sn);
				if(config == null){
					config = new ${entityName}(conf);
				}else{
					config.reset(conf);
				}
				dataMap.put(conf.get${idType}("sn"), config);
				<#list indexs as index>

				putMap(_map${index.name}, config, ${index.paramsInit});
				</#list>
			}

			// 保存数据
			_map = Collections.unmodifiableMap(dataMap);
		}

	}
    
	/**
	 *
	 * 取得属性值
	 * @key 属性名称
	 *
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T> T getFieldValue(String key) {
		Object value = null;
		switch (key) {
			<#list properties as prop>
			case "${prop.name}": {
				value = this.${prop.name};
				break;
			}
			</#list>
			default: break;
		}
		
		return (T) value;
	}

}