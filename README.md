# Lightning_MisQueen
Tugas Besar I IF2211 Strategi Algoritma Semester II Tahun 2021/2022 Pemanfaatan Algoritma Greedy dalam Aplikasi Permainan “Overdrive”

## Penjelasan Singkat Algoritma Greedy yang Diimplementasikan
Algoritma Greedy ini memiliki tingkata prioritas. Prioritasnya secara garis besar dari yang tertinggi ke yang terendah, yaitu:
- Menyelamatkan diri (jika sudah tidak bisa bergerak, selamatkan (fix) diri)
- Melakukan serangan terhadap musuh dengan <i>power up<i>
- Mempercepat pemain (melakukan akselerasi, bisa juga dengan <i>boost power up<i> jika ada)
- Memprioritaskan jalur dengan jumlah <i>power up<i> yang banyak untuk menambah poin

## Requirement Program dan Instalasi
- Instalasi Java (minimal Java 8) (https://www.oracle.com/java/technologies/downloads/#java8)
- Instalasi Intellij IDEA (https://www.jetbrains.com/idea/)
- Instalasi NodeJS (https://nodejs.org/en/download/)
- Download <i>starter-pack<i> yang terdapat pada <i>link<i> https://github.com/EntelectChallenge/2020-Overdrive/releases/tag/2020.3.4 (starter-pack.zip)

## Command atau Langkah-Langkah dalam Meng-<i>compile<i> atau <i>Build<i> Program
- Clone <i>repository<i> ini ke komputer/perangkat Anda
- <i>Unzip<i> file starter-pack.zip yang telah di-<i>download<i> pada <i>link<i> https://github.com/EntelectChallenge/2020-Overdrive/releases/tag/2020.3.4
- Masuk ke dalam folder/direktori starter-pack pada folder hasil <i>unzip<i> di atas
- Buka file game-runner-config.json
- Pada file game-runner-config.json, ubah value pada key "player-a" dari "./starter-bots/javascript" menjadi "./starter-bots/java"
- Simpan perubahan dan tutup file game-runner-config.json
- Masuk ke folder/direktori starter-bots
- Masuk ke folder/direktori java
- Hapus semua file dan folder/direktori yang berada di dalam folder/direktori java
- <i>Copy<i> semua file dan folder/direktori yang berada dalam folder/direktori src pada repository ini, lalu <i>paste<i> ke dalam folder/direktori java
- Buka Intellij IDEA pada direktori starter-pack
- Buka direktori java yang tadi di-<i>paste<i> pada intellij IDEA (langkah pembukaannya sama dengan lagkah di atas)
- Klik kanan pada file pom.xml, lalu klik Add as Maven project
- Arahkan kursor pada kotak di bagian bawah kiri Intellij IDEA
- Akan muncul daftar pilihan menu, klik Maven
- Akan muncul pilihan di kanan layar, klik java-starter-bot
- Klik Lifecycle
- Klik compile, tunggu hingga proses selesai
- Klik install, tunggu hingga proses selesai

## Menjalankan Program
- Buka folder/direktori starter-pack pada hasil di-<i>unzip<i> pada langkah di atas
- Buka file run.bat (untuk pengguna Sistem Operasi Windows)

## Author / Identitas Pembuat
Taufan Fajarama Putrawansyah R 13520031
Jevant Jedidia Augustine 13520133
David Karel Halomoan 13520154
