--<summary>
--Generated from Item.xlsx
 --int  sn
 --int[]  group
--</summary>

local array = nil
local bReadAll = false
local sortedArray = nil
local ConfBagClass = {}

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
    if sortedArray == nil then
        sortedArray = {}
    end

    for i,v in pairs(sortedArray) do
        if v[filename] == filevalue then
            return v
        end
    end

    local data = ConfBagClass.GetAllConditions(filename, filevalue)
    if data ~= nil and #data >= 1 then
       return data[1]
    end
    return nil
end

--search from db
function ConfBagClass.GetAllConditions(filename, filevalue)
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
        info = ConfBagClass.AddItem(datas[i])
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

    local arr = DB.GetTable("ConfBagClass")
    local info
    for i = 1, #arr do
        info = ConfBagClass.AddItem(arr[i])
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