--<summary>
--Generated from Item.xlsx
 --int  sn
 --int[]  group
--</summary>

local array = nil
local bReadAll = false
local sortedArray = nil
local ConfBagClass = {}

function ConfBagClass.pairs(conf)
    return pairs(conf)
end

function ConfBagClass.AddItem(db)
    return {
		sn = Config.GetData(db[1], 1),
	    group = Config.GetArrData(db[2], 21),
    }
end

function ConfBagClass.Get(id)
    if array == nil then
        array = {}
    end
    if array[id] == nil then
       local data = DB.GetData("ConfBagClass", id)
       if data ~= nil then
          array[id] = ConfBagClass.AddItem(data)
       end
    end
    return array[id]
end

function ConfBagClass.GetFirstCondition(filename, filevalue)
    if bReadAll then
        for i,v in ipairs(sortedArray) do
            if v[filename] == filevalue then
                return v
            end
        end
    end

    local data = DB.GetFirstDataCondition("ConfBagClass", filename, filevalue)
    if data == nil then
        return nil
    end

    if array == nil then
        array = {}
    end

    local info = array[Config.GetData(data[1], 1)]
    if info == nil then
        info = ConfBagClass.AddItem(data)
    end

    array[info.sn] = info
    return info
end

--search from db
function ConfBagClass.GetAllConditions(filename, filevalue)
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

    local datas = DB.GetDataCondition("ConfBagClass", filename, filevalue)

    if datas == nil then
        return nil
    end

    if array == nil then
        array = {}
    end

    local info
    local infos = {}
    for i = 1, #datas do
        info = array[Config.GetData(datas[i][1], 1)]
        if info == nil then
            info = ConfBagClass.AddItem(datas[i])
        end
        
        array[info.sn] = info
        infos[#infos + 1] = info
    end
    return infos
end

function ConfBagClass.GetAll()
    if bReadAll then
        return array
    end
    bReadAll = true

    if array == nil then
        array = {}
    end
    sortedArray = {}

    local datas = DB.GetTable("ConfBagClass")
    local info
    for i = 1, #datas do
        info = array[Config.GetData(datas[i][1], 1)]
        if info == nil then
            info = ConfBagClass.AddItem(datas[i])
        end

        array[info.sn] = info
        sortedArray[i] = info
    end
    return array
end

function ConfBagClass.GetSortedAll()
    if nil == sortedArray then
        ConfBagClass.GetAll()
    end
    return sortedArray
end

function ConfBagClass.GetByLine(line)
    return ConfBagClass.GetSortedAll()[line]
end

function ConfBagClass.GetLineCount()
    return DB.GetRowCountInTable("ConfBagClass")
end

return ConfBagClass