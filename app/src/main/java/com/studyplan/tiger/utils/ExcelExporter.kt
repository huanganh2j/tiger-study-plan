package com.studyplan.tiger.utils

import android.content.Context
import android.os.Environment
import com.studyplan.tiger.data.entity.PlanRecord
import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

/**
 * Excel导出工具类
 */
class ExcelExporter(private val context: Context) {
    
    companion object {
        private const val EXPORT_DIR = "plan_file"
    }
    
    /**
     * 导出记录到Excel文件
     */
    fun exportRecords(records: List<PlanRecord>): String? {
        try {
            // 创建工作簿
            val workbook = XSSFWorkbook()
            val sheet = workbook.createSheet("学习计划记录")
            
            // 创建样式
            val headerStyle = createHeaderStyle(workbook)
            val dataStyle = createDataStyle(workbook)
            val dateStyle = createDateStyle(workbook)
            
            // 创建表头
            createHeader(sheet, headerStyle)
            
            // 填充数据
            fillData(sheet, records, dataStyle, dateStyle)
            
            // 自动调整列宽
            for (i in 0..4) {
                sheet.autoSizeColumn(i)
            }
            
            // 保存文件
            val fileName = generateFileName()
            val file = saveWorkbook(workbook, fileName)
            
            workbook.close()
            
            return file?.absolutePath
            
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
    
    /**
     * 创建表头样式
     */
    private fun createHeaderStyle(workbook: Workbook): CellStyle {
        val style = workbook.createCellStyle()
        val font = workbook.createFont()
        
        font.bold = true
        font.fontSize = 12
        style.setFont(font)
        style.fillForegroundColor = IndexedColors.GREY_25_PERCENT.index
        style.fillPattern = FillPatternType.SOLID_FOREGROUND
        style.borderTop = BorderStyle.THIN
        style.borderBottom = BorderStyle.THIN
        style.borderLeft = BorderStyle.THIN
        style.borderRight = BorderStyle.THIN
        style.alignment = HorizontalAlignment.CENTER
        style.verticalAlignment = VerticalAlignment.CENTER
        
        return style
    }
    
    /**
     * 创建数据样式
     */
    private fun createDataStyle(workbook: Workbook): CellStyle {
        val style = workbook.createCellStyle()
        
        style.borderTop = BorderStyle.THIN
        style.borderBottom = BorderStyle.THIN
        style.borderLeft = BorderStyle.THIN
        style.borderRight = BorderStyle.THIN
        style.verticalAlignment = VerticalAlignment.CENTER
        
        return style
    }
    
    /**
     * 创建日期样式
     */
    private fun createDateStyle(workbook: Workbook): CellStyle {
        val style = createDataStyle(workbook)
        val dataFormat = workbook.createDataFormat()
        style.dataFormat = dataFormat.getFormat("yyyy-mm-dd hh:mm")
        return style
    }
    
    /**
     * 创建表头
     */
    private fun createHeader(sheet: Sheet, headerStyle: CellStyle) {
        val headerRow = sheet.createRow(0)
        val headers = arrayOf("计划日期", "事项名称", "开始时间", "结束时间", "完成状态")
        
        headers.forEachIndexed { index, header ->
            val cell = headerRow.createCell(index)
            cell.setCellValue(header)
            cell.cellStyle = headerStyle
        }
    }
    
    /**
     * 填充数据
     */
    private fun fillData(sheet: Sheet, records: List<PlanRecord>, dataStyle: CellStyle, dateStyle: CellStyle) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        
        records.forEachIndexed { index, record ->
            val row = sheet.createRow(index + 1)
            
            // 计划日期
            val dateCell = row.createCell(0)
            dateCell.setCellValue(dateFormat.format(record.planDate))
            dateCell.cellStyle = dataStyle
            
            // 事项名称
            val nameCell = row.createCell(1)
            nameCell.setCellValue(record.taskName)
            nameCell.cellStyle = dataStyle
            
            // 开始时间
            val startTimeCell = row.createCell(2)
            startTimeCell.setCellValue(timeFormat.format(record.startTime))
            startTimeCell.cellStyle = dataStyle
            
            // 结束时间
            val endTimeCell = row.createCell(3)
            endTimeCell.setCellValue(timeFormat.format(record.endTime))
            endTimeCell.cellStyle = dataStyle
            
            // 完成状态
            val statusCell = row.createCell(4)
            statusCell.setCellValue(if (record.isCompleted) "已完成" else "未完成")
            statusCell.cellStyle = dataStyle
            
            // 设置完成状态的颜色
            if (record.isCompleted) {
                val completedStyle = sheet.workbook.createCellStyle()
                completedStyle.cloneStyleFrom(dataStyle)
                val font = sheet.workbook.createFont()
                font.color = IndexedColors.GREEN.index
                completedStyle.setFont(font)
                statusCell.cellStyle = completedStyle
            } else {
                val notCompletedStyle = sheet.workbook.createCellStyle()
                notCompletedStyle.cloneStyleFrom(dataStyle)
                val font = sheet.workbook.createFont()
                font.color = IndexedColors.RED.index
                notCompletedStyle.setFont(font)
                statusCell.cellStyle = notCompletedStyle
            }
        }
        
        // 添加统计行
        addStatisticsRow(sheet, records, dataStyle)
    }
    
    /**
     * 添加统计行
     */
    private fun addStatisticsRow(sheet: Sheet, records: List<PlanRecord>, dataStyle: CellStyle) {
        val statsRowIndex = records.size + 2
        val statsRow = sheet.createRow(statsRowIndex)
        
        val totalCount = records.size
        val completedCount = records.count { it.isCompleted }
        val completionRate = if (totalCount == 0) 0f else (completedCount.toFloat() / totalCount.toFloat()) * 100
        
        // 统计标题
        val labelCell = statsRow.createCell(0)
        labelCell.setCellValue("统计汇总：")
        labelCell.cellStyle = dataStyle
        
        // 总计划数
        val totalCell = statsRow.createCell(1)
        totalCell.setCellValue("总计划：$totalCount")
        totalCell.cellStyle = dataStyle
        
        // 已完成数
        val completedCell = statsRow.createCell(2)
        completedCell.setCellValue("已完成：$completedCount")
        completedCell.cellStyle = dataStyle
        
        // 完成率
        val rateCell = statsRow.createCell(3)
        rateCell.setCellValue("完成率：${String.format("%.1f", completionRate)}%")
        rateCell.cellStyle = dataStyle
    }
    
    /**
     * 生成文件名
     */
    private fun generateFileName(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return "学习计划_${dateFormat.format(Date())}.xlsx"
    }
    
    /**
     * 保存工作簿到文件
     */
    private fun saveWorkbook(workbook: Workbook, fileName: String): File? {
        try {
            // 获取外部存储目录
            val externalDir = Environment.getExternalStorageDirectory()
            val exportDir = File(externalDir, EXPORT_DIR)
            
            // 创建目录
            if (!exportDir.exists()) {
                exportDir.mkdirs()
            }
            
            // 创建文件
            val file = File(exportDir, fileName)
            val outputStream = FileOutputStream(file)
            
            // 写入数据
            workbook.write(outputStream)
            outputStream.close()
            
            return file
            
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
    
    /**
     * 获取导出目录路径
     */
    fun getExportDirPath(): String {
        val externalDir = Environment.getExternalStorageDirectory()
        return File(externalDir, EXPORT_DIR).absolutePath
    }
}