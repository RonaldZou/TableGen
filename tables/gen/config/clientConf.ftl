--<summary>
--Generated from ${excelName}
<#list fields as field>
 --${field.type}  ${field.name}
</#list>
--</summary>

local array = nil
local bReadAll = false
local sortedArray = nil
local ${entityName} = {}
<#-- 暴力写法，有需要优化 -->
<#assign map = {"int":1, "string":2, "String":2, "float":3, "boolean":4, "bool":4,"short":5, "byte":6, "long":7, "double":8,"vector3d":9,"vector2d":10}>
<#assign map1 = {"int[]":21,  "string[]":22, "String[]":22, "float[]":23, "bool[]":24,"boolean[]":24, "short[]":25, "byte[]":26, "long[]":27, "double[]":28,"vector3d[]":29,"vector2d[]":30}>
<#assign map2 = {"int[][]":41,  "string[][]":42, "String[][]":42, "float[][]":43, "bool[]":44,"boolean[][]":44, "short[][]":45, "byte[][]":46, "long[][]":47, "double[][]":48,"vector3d[][]":49,"vector2d[][]":50}>

function ${entityName}.pairs(conf)
    return pairs(conf)
end

function ${entityName}.AddItem(db)
    return {
	<#list fields as field>
	<#if field.type?contains("[][]")>
       	${field.name} = Config.Get2ArrData(db[${field_index +1}], ${map2[field.type]}),
	<#elseif field.type?contains("[]")>
	    ${field.name} = Config.GetArrData(db[${field_index +1}], ${map1[field.type]}),
	<#else>
		${field.name} = Config.GetData(db[${field_index +1}], ${map[field.type]}),
	</#if>
	</#list>
    }
end

function ${entityName}.Get(id)
    if array == nil then
        array = {}
    end
    if array[id] == nil then
       local data = DB.GetData("${entityName}", id)
       if data ~= nil then
          array[id] = ${entityName}.AddItem(data)
       end
    end
    return array[id]
end

function ${entityName}.GetFirstCondition(filename, filevalue)
    if bReadAll then
        for i,v in ipairs(sortedArray) do
            if v[filename] == filevalue then
                return v
            end
        end
    end

    local data = DB.GetFirstDataCondition("${entityName}", filename, filevalue)
    if data == nil then
        return nil
    end

    if array == nil then
        array = {}
    end

    <#list fields as field>
    <#if field.name == "sn">
    local info = array[Config.GetData(data[${field_index +1}], ${map[field.type]})]
    if info == nil then
        info = ${entityName}.AddItem(data)
    end
    </#if>
    </#list>

    array[info.sn] = info
    return info
end

--search from db
function ${entityName}.GetAllConditions(filename, filevalue)
    if bReadAll then
        local infos = {}
        local ni = 1
        for i, item in ipairs(sortedArray) do
            if item[filename] == filevalue then
                infos[ni] = item
                ni = ni + 1
            end
        end
        return infos
    end

    local datas = DB.GetDataCondition("${entityName}", filename, filevalue)

    if datas == nil then
        return nil
    end

    if array == nil then
        array = {}
    end

    local info
    local infos = {}
    for i = 1, #datas do
        <#list fields as field>
        <#if field.name == "sn">
        info = array[Config.GetData(datas[i][${field_index +1}], ${map[field.type]})]
        if info == nil then
            info = ${entityName}.AddItem(datas[i])
        end
        </#if>
        </#list>
        
        array[info.sn] = info
        infos[#infos + 1] = info
    end
    return infos
end

function ${entityName}.GetAll()
    if bReadAll then
        return array
    end
    bReadAll = true

    if array == nil then
        array = {}
    end
    sortedArray = {}

    local datas = DB.GetTable("${entityName}")
    local info
    for i = 1, #datas do
        <#list fields as field>
        <#if field.name == "sn">
        info = array[Config.GetData(datas[i][${field_index +1}], ${map[field.type]})]
        if info == nil then
            info = ${entityName}.AddItem(datas[i])
        end
        </#if>
        </#list>

        array[info.sn] = info
        sortedArray[i] = info
    end
    return array
end

function ${entityName}.GetSortedAll()
    if nil == sortedArray then
        ${entityName}.GetAll()
    end
    return sortedArray
end

function ${entityName}.GetByLine(line)
    return ${entityName}.GetSortedAll()[line]
end

function ${entityName}.GetLineCount()
    return DB.GetRowCountInTable("${entityName}")
end

return ${entityName}