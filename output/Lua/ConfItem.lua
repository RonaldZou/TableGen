--<summary>
--Generated from Item.xlsx
 --int  sn
 --String  name
 --String  description
 --String  remarks
 --int  type
 --int  stype
 --int  levelLimit
 --int  equipJob
 --int  condition
 --int  weight
 --int  quickUse
 --boolean  autoUse
 --int  color
 --int  grade
 --int  sortindex
 --int  overlap
 --boolean  sell
 --int  sell_type
 --int  sell_ccyType
 --int  priceMin
 --int  priceMax
 --int  chillDown
 --String  iconImageId
 --String  iconSamllImageId
 --String  dropModId
 --float  scale
 --boolean  hasEffect
 --boolean  use
 --int  useConfirmStr
 --boolean  volumeUse
 --String  selectModel
 --String  param
 --int  openUI
 --String[][]  useFunction
 --int  limitSn
 --int  cdGroupSn
 --int  useFunctionCD
 --boolean  canCompose
 --boolean  commonTrade
 --int  obtain_type
 --String  typeDesc
 --String  funDesc
 --int  quality
 --int  highQualityId
 --int  groupSn
 --int  showInAddRecipe
 --int[]  disassembleConsume
 --String[]  disassemble
 --int  tarPres
 --boolean  canmail
 --int[]  mailmoney
 --int  deadDropProb
 --int  salePrice
 --int  ccyType
 --int  itemClass
 --boolean  preview
 --int  tributeVaule
 --int[]  subCareer
 --int[][]  accessWay
 --boolean  recommend
 --int[]  equipmentPic
 --int  displayStatus
--</summary>

local array = nil
local bReadAll = false
local sortedArray = nil
local ConfItem = {}

function ConfItem.pairs(conf)
    return pairs(conf)
end

function ConfItem.AddItem(db)
    return {
		sn = Config.GetData(db[1], 1),
		name = Config.GetData(db[2], 2),
		description = Config.GetData(db[3], 2),
		remarks = Config.GetData(db[4], 2),
		type = Config.GetData(db[5], 1),
		stype = Config.GetData(db[6], 1),
		levelLimit = Config.GetData(db[7], 1),
		equipJob = Config.GetData(db[8], 1),
		condition = Config.GetData(db[9], 1),
		weight = Config.GetData(db[10], 1),
		quickUse = Config.GetData(db[11], 1),
		autoUse = Config.GetData(db[12], 4),
		color = Config.GetData(db[13], 1),
		grade = Config.GetData(db[14], 1),
		sortindex = Config.GetData(db[15], 1),
		overlap = Config.GetData(db[16], 1),
		sell = Config.GetData(db[17], 4),
		sell_type = Config.GetData(db[18], 1),
		sell_ccyType = Config.GetData(db[19], 1),
		priceMin = Config.GetData(db[20], 1),
		priceMax = Config.GetData(db[21], 1),
		chillDown = Config.GetData(db[22], 1),
		iconImageId = Config.GetData(db[23], 2),
		iconSamllImageId = Config.GetData(db[24], 2),
		dropModId = Config.GetData(db[25], 2),
		scale = Config.GetData(db[26], 3),
		hasEffect = Config.GetData(db[27], 4),
		use = Config.GetData(db[28], 4),
		useConfirmStr = Config.GetData(db[29], 1),
		volumeUse = Config.GetData(db[30], 4),
		selectModel = Config.GetData(db[31], 2),
		param = Config.GetData(db[32], 2),
		openUI = Config.GetData(db[33], 1),
       	useFunction = Config.Get2ArrData(db[34], 42),
		limitSn = Config.GetData(db[35], 1),
		cdGroupSn = Config.GetData(db[36], 1),
		useFunctionCD = Config.GetData(db[37], 1),
		canCompose = Config.GetData(db[38], 4),
		commonTrade = Config.GetData(db[39], 4),
		obtain_type = Config.GetData(db[40], 1),
		typeDesc = Config.GetData(db[41], 2),
		funDesc = Config.GetData(db[42], 2),
		quality = Config.GetData(db[43], 1),
		highQualityId = Config.GetData(db[44], 1),
		groupSn = Config.GetData(db[45], 1),
		showInAddRecipe = Config.GetData(db[46], 1),
	    disassembleConsume = Config.GetArrData(db[47], 21),
	    disassemble = Config.GetArrData(db[48], 22),
		tarPres = Config.GetData(db[49], 1),
		canmail = Config.GetData(db[50], 4),
	    mailmoney = Config.GetArrData(db[51], 21),
		deadDropProb = Config.GetData(db[52], 1),
		salePrice = Config.GetData(db[53], 1),
		ccyType = Config.GetData(db[54], 1),
		itemClass = Config.GetData(db[55], 1),
		preview = Config.GetData(db[56], 4),
		tributeVaule = Config.GetData(db[57], 1),
	    subCareer = Config.GetArrData(db[58], 21),
       	accessWay = Config.Get2ArrData(db[59], 41),
		recommend = Config.GetData(db[60], 4),
	    equipmentPic = Config.GetArrData(db[61], 21),
		displayStatus = Config.GetData(db[62], 1),
    }
end

function ConfItem.Get(id)
    if array == nil then
        array = {}
    end
    if array[id] == nil then
       local data = DB.GetData("ConfItem", id)
       if data ~= nil then
          array[id] = ConfItem.AddItem(data)
       end
    end
    return array[id]
end

function ConfItem.GetFirstCondition(filename, filevalue)
    if bReadAll then
        for i,v in ipairs(sortedArray) do
            if v[filename] == filevalue then
                return v
            end
        end
    end

    local data = DB.GetFirstDataCondition("ConfItem", filename, filevalue)
    if data == nil then
        return nil
    end

    if array == nil then
        array = {}
    end

    local info = array[Config.GetData(data[1], 1)]
    if info == nil then
        info = ConfItem.AddItem(data)
    end

    array[info.sn] = info
    return info
end

--search from db
function ConfItem.GetAllConditions(filename, filevalue)
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

    local datas = DB.GetDataCondition("ConfItem", filename, filevalue)

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
            info = ConfItem.AddItem(datas[i])
        end
        
        array[info.sn] = info
        infos[#infos + 1] = info
    end
    return infos
end

function ConfItem.GetAll()
    if bReadAll then
        return array
    end
    bReadAll = true

    if array == nil then
        array = {}
    end
    sortedArray = {}

    local datas = DB.GetTable("ConfItem")
    local info
    for i = 1, #datas do
        info = array[Config.GetData(datas[i][1], 1)]
        if info == nil then
            info = ConfItem.AddItem(datas[i])
        end

        array[info.sn] = info
        sortedArray[i] = info
    end
    return array
end

function ConfItem.GetSortedAll()
    if nil == sortedArray then
        ConfItem.GetAll()
    end
    return sortedArray
end

function ConfItem.GetByLine(line)
    return ConfItem.GetSortedAll()[line]
end

function ConfItem.GetLineCount()
    return DB.GetRowCountInTable("ConfItem")
end

return ConfItem