package com.wuzuan.nfcdarktoolkit.domain.model

data class Ad(
    val title: String,
    val description: String,
    val url: String
)

object AdProvider {
    private val ads = listOf(
        Ad(
            title = "臺灣 Minecraft 平價主機",
            description = "使用3.3 GHz 的 Intel Xeon E5-2660 v3 CPU處理器｜搭配快速的 DDR4記憶體 NVMe SSD｜擁有方便的面板方便管理您的伺服器",
            url = "https://store.diamondhost.tw/store/tw-basic"
        ),
        Ad(
            title = "臺灣 Minecraft 極致主機",
            description = "採用最高 6.0GHz 的 i9-14900KF 處理器｜搭配高效 DDR5 5600 記憶體與 Gen4 NVMe SSD，讓你開大型伺服器都沒有問題！",
            url = "https://store.diamondhost.tw/store/tw-extreme"
        ),
        Ad(
            title = "新加坡 DirectAdmin 網頁主機",
            description = "採用最高 5.70GHz 的 AMD Ryzen™ 9 7950X 與DDR5 記憶體，搭配企業級 NVMe SSD",
            url = "https://store.diamondhost.tw/store/sg-directadmin"
        ),
        Ad(
            title = "美國 Discord 機器人託管",
            description = "美國低延遲機器人託管，低延遲、超快速，實際連線不到 50ms！",
            url = "https://store.diamondhost.tw/store/us-bot"
        ),
        Ad(
            title = "NordVpn",
            description = "保護您的連線並隱藏您的 IP。 阻止惡意軟體、追蹤器和廣告。",
            url = "https://store.diamondhost.tw/store/nordvpn"
        )
    )

    fun getRandomAd(): Ad {
        return ads.random()
    }
}
