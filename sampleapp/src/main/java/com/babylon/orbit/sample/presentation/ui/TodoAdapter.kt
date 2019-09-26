package com.babylon.orbit.sample.presentation.ui

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.babylon.orbit.sample.R
import com.babylon.orbit.sample.domain.todo.Todo

class TodoAdapter(
    private val todoSelected: (todoId: Int) -> Unit,
    private val todoUserSelected: (userId: Int) -> Unit
) : RecyclerView.Adapter<TodoViewHolder>() {

    var todoItems: List<Todo> = emptyList()
    set(value) {
        field = value
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.viewholder_todo_item, parent, false
        )
        return TodoViewHolder(view)
    }

    override fun getItemCount() = todoItems.size

    override fun onBindViewHolder(holder: TodoViewHolder, position: Int) {
        holder.itemView.setOnClickListener {
            todoSelected(todoItems[holder.adapterPosition].id)
        }
        holder.itemView.setOnLongClickListener {
            todoUserSelected(todoItems[holder.adapterPosition].userId)
            true
        }
        holder.bind(todoItems[position])
    }
}

class TodoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val todoIdTextView = itemView.findViewById<TextView>(R.id.todoId)
    private val userIdTextView = itemView.findViewById<TextView>(R.id.userId)
    private val todoTitleTextView = itemView.findViewById<TextView>(R.id.todoTitle)

    @SuppressLint("SetTextI18n")
    fun bind(todo: Todo) {
        todo.apply {
            todoIdTextView.text = TODO_ID_PREFIX + id
            userIdTextView.text = TODO_USER_ID_PREFIX + userId
            todoTitleTextView.text = title
        }
    }

    private companion object {
        const val TODO_ID_PREFIX = "The todo id is "
        const val TODO_USER_ID_PREFIX = "The user's todo id is "
    }
}
