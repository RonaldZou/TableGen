--<summary>
--Generated from ServerInfo.xlsx
 --int  sn
 --int  serverId
 --String  type
 --String  name
 --String  ip
 --int  clientPort
 --int  recommend
 --int  status
 --int  order
--</summary>

local array = nil
local bReadAll = false
local sortedArray = nil
local ConfServerInfo = {}

function ConfServerInfo.pairs(conf)
    return pairs(conf)
end

function ConfServerInfo.AddItem(db)
    return {
		sn = Config.GetData(db[1], 1),
		serverId = Config.GetData(db[2], 1),
		type = Config.GetData(db[3], 2),
		name = Config.GetData(db[4], 2),
		ip = Config.GetData(db[5], 2),
		clientPort = Config.GetData(db[6], 1),
		recommend = Config.GetData(db[7], 1),
		status = Config.GetData(db[8], 1),
		order = Config.GetData(db[9], 1),
    }
end

function ConfServerInfo.Get(id)
    if array == nil then
        array = {}
    end
    if array[id] == nil then
       local data = DB.GetData("ConfServerInfo", id)
       if data ~= nil then
          array[id] = ConfServerInfo.AddItem(data)
       end
    end
    return array[id]
end

function ConfServerInfo.GetFirstCondition(filename, filevalue)
    if bReadAll then
        for i,v in ipairs(sortedArray) do
            if v[filename] == filevalue then
                return v
            end
        end
    end

    local data = DB.GetFirstDataCondition("ConfServerInfo", filename, filevalue)
    if data == nil then
        return nil
    end

    if array == nil then
        array = {}
    end

    local info = array[Config.GetData(data[1], 1)]
    if info == nil then
        info = ConfServerInfo.AddItem(data)
    end

    array[info.sn] = info
    return info
end

--search from db
function ConfServerInfo.GetAllConditions(filename, filevalue)
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

    local datas = DB.GetDataCondition("ConfServerInfo", filename, filevalue)

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
            info = ConfServerInfo.AddItem(datas[i])
        end
        
        array[info.sn] = info
        infos[#infos + 1] = info
    end
    return infos
end

function ConfServerInfo.GetAll()
    if bReadAll then
        return array
    end
    bReadAll = true

    if array == nil then
        array = {}
    end
    sortedArray = {}

    local datas = DB.GetTable("ConfServerInfo")
    local info
    for i = 1, #datas do
        info = array[Config.GetData(datas[i][1], 1)]
        if info == nil then
            info = ConfServerInfo.AddItem(datas[i])
        end

        array[info.sn] = info
        sortedArray[i] = info
    end
    return array
end

function ConfServerInfo.GetSortedAll()
    if nil == sortedArray then
        ConfServerInfo.GetAll()
    end
    return sortedArray
end

function ConfServerInfo.GetByLine(line)
    return ConfServerInfo.GetSortedAll()[line]
end

function ConfServerInfo.GetLineCount()
    return DB.GetRowCountInTable("ConfServerInfo")
end

return ConfServerInfo