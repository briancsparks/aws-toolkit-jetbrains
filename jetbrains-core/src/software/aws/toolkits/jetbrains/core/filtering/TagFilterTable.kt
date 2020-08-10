// Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package software.aws.toolkits.jetbrains.core.filtering

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.execution.util.ListTableWithButtons
import com.intellij.openapi.project.Project
import com.intellij.ui.TextFieldWithAutoCompletion
import com.intellij.ui.TextFieldWithAutoCompletion.StringsCompletionProvider
import com.intellij.ui.TextFieldWithAutoCompletionListProvider
import com.intellij.util.ui.AbstractTableCellEditor
import com.intellij.util.ui.ColumnInfo
import com.intellij.util.ui.ListTableModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import software.amazon.awssdk.services.resourcegroupstaggingapi.ResourceGroupsTaggingApiClient
import software.aws.toolkits.jetbrains.core.awsClient
import software.aws.toolkits.jetbrains.services.ecs.execution.ArtifactMapping
import software.aws.toolkits.jetbrains.utils.ApplicationThreadPoolScope
import java.awt.Component
import java.util.EventObject
import javax.swing.AbstractCellEditor
import javax.swing.DefaultCellEditor
import javax.swing.JTable
import javax.swing.event.CellEditorListener
import javax.swing.table.DefaultTableCellRenderer
import javax.swing.table.TableCellEditor
import javax.swing.table.TableCellRenderer

class TagFilterTable(private val project: Project) : ListTableWithButtons<TagFilterTableModel>() {
    class KeyProvider(project: Project) :
        StringsCompletionProvider(listOf(), null),
        CoroutineScope by ApplicationThreadPoolScope("completionProvider") {

        init {
            launch {
                val client: ResourceGroupsTaggingApiClient = project.awsClient()
                val items = client.tagKeysPaginator.tagKeys().toMutableList()
                setItems(items)
            }
        }
    }

    init {
        tableView.tableHeader.reorderingAllowed = false
        tableView.autoResizeMode = JTable.AUTO_RESIZE_LAST_COLUMN
    }

    override fun createListModel(): ListTableModel<*> = ListTableModel<ArtifactMapping>(
        object : ColumnInfo<TagFilterTableModel, Boolean>("TODO enabled") {
            override fun getColumnClass(): Class<*> = Boolean::class.java
            override fun valueOf(item: TagFilterTableModel): Boolean? = item.enabled
            override fun isCellEditable(item: TagFilterTableModel): Boolean = true
            override fun setValue(item: TagFilterTableModel, value: Boolean?) {
                item.enabled = value ?: false
            }
        },
        object : ColumnInfo<TagFilterTableModel, String>("TODO key") {
            override fun valueOf(item: TagFilterTableModel): String? = item.key
            override fun isCellEditable(item: TagFilterTableModel): Boolean = true
            override fun setValue(item: TagFilterTableModel, value: String?) {
                item.key = value
            }

            override fun getEditor(item: TagFilterTableModel?): TableCellEditor? {
                return object : AbstractTableCellEditor() {
                    val field = TextFieldWithAutoCompletion(project, KeyProvider(project), false, item?.key ?: "")
                    override fun getCellEditorValue(): Any {
                        return field.text
                    }

                    override fun getTableCellEditorComponent(table: JTable?, value: Any?, isSelected: Boolean, row: Int, column: Int): Component {
                        return field
                    }
                }
            }
        },
        object : ColumnInfo<TagFilterTableModel, String>("TODO value") {
            override fun valueOf(item: TagFilterTableModel): String? = item.values.joinToString(", ")
            override fun isCellEditable(item: TagFilterTableModel): Boolean = true
            override fun setValue(item: TagFilterTableModel, value: String) {
                item.values = value.split(",").map { it.trim() }
            }
        }
    )

    fun getItems() = elements.toList()

    override fun createElement(): TagFilterTableModel = TagFilterTableModel()

    override fun isEmpty(element: TagFilterTableModel?): Boolean = element?.key.isNullOrEmpty() || element?.values.isNullOrEmpty()

    override fun cloneElement(variable: TagFilterTableModel): TagFilterTableModel = variable.copy()

    override fun canDeleteElement(selection: TagFilterTableModel): Boolean = true
}

data class TagFilterTableModel(
    var enabled: Boolean = true,
    var key: String? = null,
    var values: List<String> = listOf()
)

