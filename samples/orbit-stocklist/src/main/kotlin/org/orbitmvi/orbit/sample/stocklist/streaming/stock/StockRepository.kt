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

package org.orbitmvi.orbit.sample.stocklist.streaming.stock

import android.text.format.DateUtils
import com.lightstreamer.client.ItemUpdate
import com.lightstreamer.client.Subscription
import com.lightstreamer.client.SubscriptionListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import org.orbitmvi.orbit.sample.stocklist.streaming.EmptySubscriptionListener
import org.orbitmvi.orbit.sample.stocklist.streaming.StreamingClient
import java.math.RoundingMode
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Suppress("MagicNumber", "ComplexCondition")
class StockRepository(private val client: StreamingClient) {

    private val items = (1..20).map { "item$it" }.toTypedArray()
    private val subscriptionFields = arrayOf("stock_name", "bid", "ask", "timestamp")
    private val detailSubscriptionFields =
        arrayOf("stock_name", "timestamp", "pct_change", "bid_quantity", "bid", "ask", "ask_quantity", "min", "max")

    @Suppress("RemoveExplicitTypeArguments")
    fun stockList(): Flow<List<Stock>> = callbackFlow<List<Stock>> {
        val stockList = MutableList<Stock?>(20) { null }

        trySend(emptyList())

        val subscription = Subscription("MERGE", items, subscriptionFields).apply {
            dataAdapter = "QUOTE_ADAPTER"
            requestedMaxFrequency = "1"
            requestedSnapshot = "yes"
            addListener(
                object : SubscriptionListener by EmptySubscriptionListener {
                    override fun onItemUpdate(p0: ItemUpdate) {
                        val itemName = p0.itemName
                        val stockName = p0.getValue("stock_name")
                        val formattedBid = p0.getValue("bid")?.to2dp()
                        val formattedAsk = p0.getValue("ask")?.to2dp()
                        val formattedTimestamp = p0.getValue("timestamp")?.toFormattedTimestamp()

                        if (itemName != null && stockName != null && formattedBid != null && formattedAsk != null &&
                            formattedTimestamp != null
                        ) {
                            stockList[p0.itemPos - 1] = Stock(itemName, stockName, formattedBid, formattedAsk, formattedTimestamp)
                            trySend(stockList.filterNotNull())
                        }
                    }
                }
            )
        }

        client.addSubscription(subscription)

        awaitClose {
            client.removeSubscription(subscription)
        }
    }

    @Suppress("RemoveExplicitTypeArguments")
    fun stockDetails(itemName: String): Flow<StockDetail> = callbackFlow<StockDetail> {
        val subscription = Subscription("MERGE", itemName, detailSubscriptionFields).apply {
            dataAdapter = "QUOTE_ADAPTER"
            requestedSnapshot = "yes"

            addListener(
                object : SubscriptionListener by EmptySubscriptionListener {
                    override fun onItemUpdate(p0: ItemUpdate) {
                        val stockName = p0.getValue("stock_name")
                        val pctChange = p0.getValue("pct_change")?.to2dp()
                        val formattedBid = p0.getValue("bid")?.to2dp()
                        val bidQuantity = p0.getValue("bid_quantity")
                        val formattedAsk = p0.getValue("ask")?.to2dp()
                        val askQuantity = p0.getValue("ask_quantity")
                        val min = p0.getValue("min")?.to2dp()
                        val max = p0.getValue("max")?.to2dp()
                        val formattedTimestamp = p0.getValue("timestamp")?.toFormattedTimestamp()

                        if (stockName != null &&
                            pctChange != null &&
                            formattedBid != null &&
                            bidQuantity != null &&
                            formattedAsk != null &&
                            askQuantity != null &&
                            min != null &&
                            max != null &&
                            formattedTimestamp != null
                        ) {
                            val stock = StockDetail(
                                itemName = itemName,
                                name = stockName,
                                pctChange = pctChange,
                                bid = formattedBid,
                                bidQuantity = bidQuantity,
                                ask = formattedAsk,
                                askQuantity = askQuantity,
                                min = min,
                                max = max,
                                timestamp = formattedTimestamp
                            )
                            trySend(stock)
                        }
                    }
                }
            )
        }

        client.addSubscription(subscription)

        awaitClose {
            client.removeSubscription(subscription)
        }
    }

    fun String.to2dp(): String = toBigDecimal().setScale(2, RoundingMode.HALF_UP).toPlainString()

    fun String.toFormattedTimestamp(): String = toLong().let { rawTimestamp ->
        if (DateUtils.isToday(rawTimestamp)) {
            DateTimeFormatter.ofLocalizedTime(FormatStyle.MEDIUM)
            LocalDateTime.ofInstant(Instant.ofEpochMilli(rawTimestamp), ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("HH:mm:ss"))
        } else {
            LocalDateTime.ofInstant(Instant.ofEpochMilli(rawTimestamp), ZoneId.systemDefault())
                .format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT))
        }
    }
}
