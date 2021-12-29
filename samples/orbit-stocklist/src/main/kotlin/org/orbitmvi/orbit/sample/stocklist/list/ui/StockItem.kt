/*
 * Copyright 2021 Mikołaj Leszczyński & Appmattus Limited
 * Copyright 2020 Babylon Partners Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * File modified by Mikołaj Leszczyński & Appmattus Limited
 * See: https://github.com/orbit-mvi/orbit-mvi/compare/c5b8b3f2b83b5972ba2ad98f73f75086a89653d3...main
 */

package org.orbitmvi.orbit.sample.stocklist.list.ui

import android.view.View
import com.xwray.groupie.Item
import com.xwray.groupie.viewbinding.BindableItem
import org.orbitmvi.orbit.sample.stocklist.R
import org.orbitmvi.orbit.sample.stocklist.databinding.ListItemBinding
import org.orbitmvi.orbit.sample.stocklist.list.business.ListViewModel
import org.orbitmvi.orbit.sample.stocklist.streaming.stock.Stock

class StockItem(
    private val stock: Stock,
    private val listViewModel: ListViewModel
) : BindableItem<ListItemBinding>() {

    private val bidRef = JobHolder()
    private val askRef = JobHolder()

    override fun initializeViewBinding(view: View) = ListItemBinding.bind(view)

    override fun getLayout() = R.layout.list_item

    override fun isSameAs(other: Item<*>) = other is StockItem && stock.name == other.stock.name
    override fun hasSameContentAs(other: Item<*>) = other is StockItem && stock == other.stock

    override fun bind(viewBinding: ListItemBinding, position: Int) {
        if (viewBinding.name.text == stock.name) {
            animateChange(viewBinding.bid, viewBinding.bidTick, stock.bid, bidRef)
            animateChange(viewBinding.ask, viewBinding.askTick, stock.ask, askRef)
        } else {
            hideTicks(viewBinding)

            viewBinding.root.setOnClickListener {
                listViewModel.viewMarket(stock.itemName)
            }
        }

        viewBinding.name.text = stock.name
        viewBinding.bid.text = stock.bid
        viewBinding.ask.text = stock.ask
        viewBinding.timestamp.text = stock.timestamp
    }

    private fun hideTicks(viewBinding: ListItemBinding) {
        bidRef.job?.cancel()
        askRef.job?.cancel()
        viewBinding.bidTick.visibility = View.INVISIBLE
        viewBinding.askTick.visibility = View.INVISIBLE
    }
}
