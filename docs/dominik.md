Notatki z części kodu aplikacji - łączenie się bleutooth z urządzeniem


Polecam pobrać sobie Android Studio, a nawet zainstalować sobie maszynę wirtualną. IDE powinno samo wykryć ustawienia i zainstalować sobie potrzebne SKD, ale jak nie to projekt używa:

```json
{
	"GradleVersion": "8.2.1",
	"Android studio": "Iguana",
	"Android SDK": "14 - \"Upside Down Cake\"",
	"ApiLevel": "34, revision 2",
	"Język": "Kotlin",
}
```

Json tylko aby to ładnie wyświetlić w Markdown, nie ma zastosowania w projekcie.

Polecam sobie na bierząco przeglądać te pliki co tutaj wymieniam. Nie będę się rozpisywał o każdym szczególe, bo to nie ma sensu. Wszystko jest opisane w dokumentacji Androida, a ja opiszę co jest ważne i co warto znać. Kod zawiera też komentarze.

#Struktura

Ponizej opiszę strukturę plików i folderów w aplikacji. Taki widok prezentuje się w Android Studio, ale nie fizycznie w ułożeniu folderów. Niemniej jednak


![MarineGEO circle logo](./1.png "Ogół struktury część 1")

### Manifests
`AndroidManifest.xml` - plik do którego odwołuje się system Android. Zawiera informacje o aplikacji, takie jak nazwa, ikona, wersja, uprawnienia, itp. . Najwarzniejsze są chyba uprawnienia. Dodatkowo jakbyśmy chcieli tą apkę dać na Google Store to trzeby się szczegółowo zainteresować tym plikiem.

### Kotlin+Java

Tu znajduje się cały kod aplikacji, nasza używa w 100% kotlina. Opisz szczeółowy będzie w kolejnym rozdziale. Struktura plików:

- `android.iot` - Główny folder aplikacji. Znajdują się tam wszyskie aktywności aplikacji.

- `android.iot.MainActivity.kt` - Główna aktywność włączana przy starcie aplikacji. Zawiera menu główne aplikacji. T.J. 2 przyciski do zmiany tego widoku.

- `android.iot.UserDevices.kt` - Aktywność do zarządzania listą sparowanych urządzeń danego urzytkownika. Wyświtla ją poprzez call do API.

- `android.iot.AccountActivity.kt` - Michał suff....

- `android.iot.LoginActivity.kt` - Michał suff....

- `android.iot.RegisterActivity.kt` - Michał suff....

- `android.iot.DeviceReadingActivity.kt` - Michał suff....

- `android.iot.PairedDeviceListActivity.kt` - Aktywność do wyłapania urządzeń bluetooth w pobliżu. Wyświetla je w liście i pozwala na sparowanie z nimi.

- `android.iot.BluetoothAddDeviceActivity.kt` - Aktywność do sparowania urządzenia z użytkownikiem. Wysyła do płytki ESP-32 dane do sparowania.

- `android.iot.lists` - Folder z klasami, które są potrzebne, aby dynamicznie tworzyć listy (*RecyclerViewandroidx.recyclerview.widget.RecyclerView*).

- `android.iot.lists.bluetooth` - Klasy potrzebne do zarządzania listą w layoutcie *activity_paired_device_list.xml* . 

- `android.iot.secret` - Folder z klasami, do enkrypcji - zawirają proste funkcje (metody / zmienne statyczne - companion object) do pracy z szyfrowaniem i deszyfrowaniem.

- `android.iot.secret.Encryption.kt` - Plik z algorytmami enkrypcji.

- `android.iot.secret.SHA256.kt` - Plik z plik z małą klasą do generacji hashy SHA256.


Tutaj są jeszcze testy, ale nie będę się nimi zajmował. (androidTest ....) W skrócie nie są zrobione, bo nie były wymagane


![MarineGEO circle logo](./2.png "Ogół struktury część 2")

W folderze drawable jest coś ala miaeszaniana formatu svg oraz css, t.j style np. do przecisków i tak dalej

W folderze layout jes xml odpowiedzalny za struckutrę UI. W skrócie to jest to, co widzimy na ekranie. Jest to coś ala html ze strukturą

![MarineGEO circle logo](./3.png "Ogół struktury część 3")

W values są 2 pliki:
-	`colors.xml` - Zawiera kolory używane w aplikacji. Wartości są w formacie HEX.
-	`strings.xml` - Zawiera stringi używane w aplikacji. Wartości są w formacie UTF-8. Może się to przydać do tłumaczenia aplikacji na inne języki.

## Permisje

!!! Kod do permisji jest tylko w `PairedDeviceListActivity.kt`, `UserListDeviceAdapter.kt` i `BluetoothAddDeviceActivity.kt` !!!

Aplikacja wymaga uprawnień do działania. Są one wymagane przez system Android. W pliku `AndroidManifest.xml` znajduje się lista uprawnień, które są wymagane. Są to:

-	`BLUETOOTH` - Uprawnienie do korzystania z Bluetooth
-	`BLUETOOTH_CONNECT` - Uprawnienie do łączenia się z bluetooth
-	`BLUETOOTH_SCAN` - Uprawnienie do skanowania i identyfikacji pobliskich urządzeń. 

Jeżeli będziemy próbować użyć jakiegoś kodu, który wymaga uprawnień to aplikacja wyrzuci Security Exception i się wykrzaczy.

Jak prosimy o permisje? Właśnie tak:

```kotlin
if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_DENIED) {
	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
		ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.BLUETOOTH_CONNECT), 2)
		Toast.makeText(
			this, "Please enable bluetooth connect to add device!", Toast.LENGTH_LONG
		).show()
		finish()
		return
	}
}
```

Breakdown o co biega tutaj:

- Ten if sprawdza, czy permisja jest przyznana poprzez wywoałanie mteody `checkSelfPermission` z parametrem `android.Manifest.permission.BLUETOOTH_CONNECT`. Jeżeli jest przyznana to zwraca `PackageManager.PERMISSION_GRANTED`, jeżeli nie to `PackageManager.PERMISSION_DENIED`.
	```kotlin
	if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_DENIED) {
		// Body
	}
	```

- Ten if sprawdza, czy nasze SDK wpiera (chyba) pytanie usera o permisje. To jest podobne jak sprawdzanie na poziomie #define w CPP, statycznie podanych spraw w kompilacji. Sprawdzamy, czy nasze SKD nie ma w nazwie 29 (nasze zawsze ma 34), ale kod się nie skompiluje bez tego XDD
	```kotlin
	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
		// Body
	}
	```

- Tutaj metodą `requestPermissions` wyświetlamy prompta urzytkownikowi o daną permisję i nieważne co odpowie wychodzimy. Dlaczego nie idziemy dalej jak przyzna nam permisję? Bo tam trzeba by zutylizować to 2 na końcu wywołania meody `requestPermissions`. Nie wiem jak to działa. Ludzie też tak mają na stack overflow. Oraz ponieważ nie ma żadnej straty z tego podejścia, ponieważ urzytkownik sobie włączy ponownie jak będzie chciał i jeżeli dalej nie przyzna permisji to wracamy do punktu wyjścia.
	```kotlin
	ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.BLUETOOTH_CONNECT), 2)
	Toast.makeText(
		this, "Please enable bluetooth connect to add device!", Toast.LENGTH_LONG
	).show()
	finish()		//	Finish zamyka aplikację
	return
	```

- Jak chcemy sobie poprosić o inne permisje to wysatrcy podmienić `BLUETOOTH_CONNECT` na coś innego np. `BLUETOOTH` lub `BLUETOOTH_SCAN`.

- Tam są jeszcze inne ale to musi Michał opisać, bo nie wiem jak działają permisje do Internetu ...

## Bluetooth

Zakażdym razem, gdy checemy coś zrobić z komunikacją Bluetooth to będziemy potrzebować objektu klasy `BluetoothAdapter`, tutaj jest kod, który pozwoli nam go uzyskać:

```kotlin
val bluetoothManager: BluetoothManager = getSystemService(BluetoothManager::class.java)
val bluetoothAdapter: BluetoothAdapter = bluetoothManager.adapter ?: run {
	//  Device doesn't support Bluetooth
	Toast.makeText(
		this, "Your device does not have Bluetooth!", Toast.LENGTH_LONG
	).show()
	finish()
	return
}
```

Breakdown:
-	`getSystemService(BluetoothManager::class.java)` - Metoda, która zwraca nam objekt klasy `BluetoothManager`, poprzez odowłanie się do odpowiedniego serwisu.
-	`bluetoothManager.adapter` - Metoda, która zwraca nam objekt klasy `BluetoothAdapter`, poprzez odowłanie się do odpowiedniego serwisu. Jeżeli urządzenie nie wspiera bluetooth to zwraca `null`. Nie możemy z tym nic zrobić, więc robimy `run { }`, aby zakończyć działanie aplikacji XD.

Następnie sprawdzamy, czy użytkownik włączył bluetooth. Jeżeli nie to prosimy go o włączenie. Możliwe też jest, że apka, nie ma permisji o proszenie o bluetooth, bo użytkownik się nie zgodził, więc też musimy to sprawdzić. Kod:

```kotlin
//  Check if bluetooth is enabled, if not inform / ask the user to enable it.
if (!bluetoothAdapter.isEnabled) {
	if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_DENIED) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
			ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.BLUETOOTH_CONNECT), 2)
			Toast.makeText(
				this, "Please enable bluetooth connect to add device!", Toast.LENGTH_LONG
			).show()
			finish()
			return
		}
	}
	ActivityCompat.startActivityForResult(this, Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), 1, null)
}
```

### Bluetooth Scan

!!! Kod do discovery jest tylko w `PairedDeviceListActivity.kt` !!!

Aby móc w aktywności `PairedDeviceListActivity.kt` wyświetlić listę urządeń w pobliżu musimy zacząć proces discovery na naszym adapterze bluetooh. Proces discovery to proces skanowania urządzeń w pobliżu (Wymaga permisji `BLUETOOTH_SCAN`).

Kluczowe informacje o discovery:
-	Rozpoczęcie jest asynchroniczne, tj. `adapter.startDisovery()` zwróci nam odrazu i nie zablokuje głównego threda.
-	Discovery trwa `12 sekund`, po tym czasie jest automatycznie wyłączany.
-	Discovery można wyłączyć wcześniej poprzez wywołanie `adapter.cancelDiscovery()`.
-	Discovery można wyłączyć poprzez wyłączenie bluetooth na urządzeniu.
-	Discovery jest bardzo zasobożerny dla naszego nadajnika bluetooth, więc nie powinno się go używać zbyt często. Ale też nie powinno się go używać podczas próby łączenia z innymi urządzeniami, ponieważ mogą pakiety nie dojść lub połączenie może działać wolno.
-	Discovery nie jest wymagany do połączenia z innym urządzeniem, ale jest wymagany do identyfikacji urządzenia, które chcemy sparować.
- 	Całość działa bardzo podobnie jak zapytanie ARP w sieciach t.j. nasz nadajnik wysyła zapytanie do wszystkich urządzeń w pobliżu, a one odpowiadają. W odpowiedzi dostajemy adresy MAC urządzeń, które są w pobliżu. Wtedy możemy zidentyfikować urządzenie, które chcemy sparować. Dlatego nie ma tej nazwy urządzenia, tylko jego adres MAC (Narzekałem na to kiedyśtam).
-	Więcej info: `https://developer.android.com/develop/connectivity/bluetooth/find-bluetooth-devices#discover-devices`

Zabezpieczenia dla discovery:

-	Za każdym razem kiedy wywołuje `socket.connect()` dla socketu bluetooth, przed tą akcją robie `bluetoothAdapter.cancelDiscovery()`, aby pakiety doszły i nie było problemów z połączeniem.
-	Zawsze instrukcja przed `bluetoothAdapter.startDiscovery()` wywołuje kod poniżej, aby 2 razy tego nie wywołać w głupi sposób:
	```kotlin
	if (bluetoothAdapter.isDiscovering()) {
		bluetoothAdapter.cancelDiscovery()
	}
	```

Ok. Mamy discovery, ale jak uzyskujemy informacje o znalezionych urządzeniach????

W tym celu tworzymy objekt abstrakyjnej klasy `BroadcastReceiver` i overridujemy metodę `onReceive(context: Context, intent: Intent)`. Patrz linia 106 - `PairedDeviceListActivity.kt`.

Musimy jeszcze powiedzieć systemowi, na jakie akcje w komunikacji nasz reciver ma reagować. Robimy to poprzez wywołanie metody `registerReceiver(receiver, filter)`. Patrz linia 150 - `PairedDeviceListActivity.kt`. Filter pozwala na akcje:

Akcje:
-	`BluetoothDevice.ACTION_FOUND` - Urządzenie zostało znalezione w wyniku discovery.
-	`BluetoothAdapter.ACTION_DISCOVERY_STARTED` - Discovery zostało rozpoczęte. Używane do przekazania informacji dla end-usera.
-	`BluetoothAdapter.ACTION_DISCOVERY_FINISHED` - Discovery zostało zakończone. Używane do przekazania informacji dla end-usera.

Na koniec dla bezpieczeństwa wywołujemy `unregisterReceiver(receiver)` w metodzie `onDestroy()`.

### Bluetooth Connect

!!! Kod do łączenia się przez sockety bluetooth jest tylko w `BluetoothAddDeviceActivity.kt` i `UserListDeviceAdapter.kt` !!!

Dobry artykuł: (Nas z niego obchodzi `Connect as a client`)

`https://developer.android.com/develop/connectivity/bluetooth/connect-bluetooth-devices#kotlin`


Procedura łączenoia jest banalnie prosta i do złudzenia przypomina sockket TCP/IP z Javy. W skrócie:

Najpierw musimy uzyskać obiekt urządzenia, z którym chcemy się połączyć. Robimy to poprzez wywołanie metody `getRemoteDevice(address: String)` na naszym adapterze bluetooth. W parametrze podajemy adres MAC urządzenia, z którym chcemy się połączyć. Adres MAC możemy uzyskać poprzez discovery lub zapisany w bazie danych.
```kotlin
val device = bluetoothAdapter.getRemoteDevice(/* ADDRESS MAC NADAJNIKA BLUETOOTH*/)
```

Tworzymy sobie cosket RFCOMM poprzez wywołanie metody `createRfcommSocketToServiceRecord(uuid: UUID)` na obiekcie urządzenia. W parametrze podajemy UUID usługi, z którą chcemy się połączyć. UUID usługi możemy dostać z poprzedniego punktu. Tworzymy secure socket, ponieważ nie chcemy, aby ktoś podsłuchiwał nasze dane. W skrócie UUID to jest identyfikator usługi, która jest dostępna na urządzeniu. Więcej info: `https://developer.android.com/reference/android/bluetooth/BluetoothDevice#createRfcommSocketToServiceRecord(java.util.UUID)`.

```kotlin
val socket = device.createRfcommSocketToServiceRecord(device.uuids[0].uuid)
```

Dalej łączymy się z socketem, pamiętając o zamknięciu discovery. I pozamykaniu wszyskitego na końcu.

```kotlin
//	Jak ktoś jest expertem Socketów i widzi jawny błąd to prosze o komentarz.
//	Ja nie mam pojęcia co robie i 2 raz w życiu używam socketów na poważnie XD
try {
	bluetoothAdapter.cancelDiscovery()
	socket.connect()
	if (socket.isConnected) {
		val writer: OutputStream = socket.outputStream
		writer.write("NASZ STRING DANYCH".toByteArray())

		val reader = socket.inputStream
		val buffer = ByteArray(8192) // or 4096, or more
		val length = reader.read(buffer)
		val text = String(buffer, 0, length)
		//	text == "NASZ STRING DANYCH"

		//	Zamknięcie wszystkiego co się da
		writer.close()
		reader.close()
		socket.close()
	}
} catch (e: Exception) {
	//	Najczęściej to BrokenPipeException, gdy ktoś zamknie połączenie za wcześnie.
}
```

Warto pamiętać, że dla naszej biblioteki (od strony płytki) do łączenia się przez Bluetooth wszystkie wejścia trzeba zakończyć literką ASCII (0x0A). W skrócie na końcu trzeba zawsze dać Char(10), bo inaczej będzie zatrzymanie pracy płytki i mamy dead-lock apki i płytki. Przykład:

```kotlin
("STRING BLA BLA BAL" + Char(10)).toByteArray()		//	W writer.write(....)
```

#### Connect to device (bind it to user) - `BluetoothAddDeviceActivity.kt`

Komunikacja z płytką ESP-32 do bindingu urządzenia z użytkownikiem. W skrócie:

-	wysyłamy taki .json (oczywiście w rzeczywistości zminifikowany i zenkryptowany):
	```json
	{
		"device_id": "${Device ID}",
		"username": "${Username}",
		"ssid": "${Wifi SSID}",
		"password": "${Wifi Password}",
		"hash": "${Random unique Hash}"
	}
	```
	Skład:
	-	device_id - ID urządzenia przydzielone przez API
	-	username - Nazwa urzytkownika, tak aby płytki ktoś nie mógł sobie ukraść przy próby parowania.
	-	ssid - SSID sieci wifi podane przez użytkownika.
	-	password - Password Sieci Wifi, podane przez Użytkownika.
	-	hash - losowy hash SHA256 z `System.currentTimeMillis().toString()`, gwarantujący niepowtarzalność komunikatu.

-	Następnie płytka prztwraza komunikat i wysyła jeden z stringów jako komunikaty. Struktura odpowiedzi to json, którego dekryptujemy:
	```json
	{
		"message": "Treść wiadomości",
		"key": "Losowa liczba z bardzo dużego zakresu",
	}
	```
	-	Możlwie wartości `message`:
		-	`Ok` - Wszystko zaszło poprawnie.
		-	`Bad password` - Albo Hasło, albo SSID nie działa i płytka nie może się połączyć do sieci Wifi.
		-	`Device already has an owner` - Aby nie dało się przypisać 2 razy do tego samego urzytkownika i nadpisać ownera.
		-	`Cokolwiek innego - jakikolwiek string` - Błąd nie przewidzany przeze mnie lub Szymona XD.

#### Connect to device (unbind it from user) - `UserListDeviceAdapter.kt`

-	wysyłamy taki .json (oczywiście w rzeczywistości zminifikowany i zenkryptowany):
	```json
	{
		"username": "${this.username}",
		"doReadValue": false,
		"key": "${Random unique Hash}"
	}
	```
	Skład:
	-	username - Nazwa urzytkownika, tak aby płytki ktoś nie mógł sobie ukraść przy próbie odparowania XD.
	-	doReadValue - Marna pozostałość po pięknym systemie kontroli sterowania płytki XD. Podobno dalej płytka sprawdza czy tu ktoś nie wstałił `true` XD.
	-	key - losowy hash SHA256 z `System.currentTimeMillis().toString()`, gwarantujący niepowtarzalność komunikatu.

-	Następnie płytka prztwraza komunikat i wysyła jeden z stringów jako komunikaty. Struktura odpowiedzi to json, którego dekryptujemy:
	```json
	{
		"message": "Treść wiadomości",
		"key": "Losowa liczba z bardzo dużego zakresu",
	}
	```
	-	Możlwie wartości `message`:
		-	`Ok` - Wszystko zaszło poprawnie.
		-	`Bad password` - Albo Hasło, albo SSID nie działa i płytka nie może się połączyć do sieci Wifi. (Nie wiem jaki ma to sens przy odparowaniu, ale jest)
		-	`Device already has an owner` - Aby nie dało się przypisać 2 razy do tego samego urzytkownika i nadpisać ownera. (Nie wiem jaki ma to sens przy odparowaniu, ale jest)
		-	`Cokolwiek innego - jakikolwiek string` - Błąd nie przewidzany przeze mnie lub Szymona XD.

## Enkrypcja

Enkrypcja to jest jeden wielki Burdel, ale działa XD. Generalnie używamy enkrypcji symetrycznej (hasło to samo po obu stronach komunikacji).

### SHA256 - `java/android/iot/secret/SHA256.kt`

Używany tylko przez aplikację do generowania losowych hashy. Nie jest używany do enkrypcji.

### AES128 - `java/android/iot/secret/Encryption.kt`

Nasz warian algorytmu `AES` to: `AES128/CBC/PKCS5Padding`. Dlaczego?
-	128 - Długość klucza w bitach, czyli hasło do enkrypcji i dekrypcji ma 16 znaków (16 bajtów).
-	CBC - Tryb szyfrowania, który jest bezpieczniejszy od ECB. Więcej info: `https://pl.wikipedia.org/wiki/Block_cipher_mode_of_operation#CBC_(Cipher_Block_Chaining)`
-	PKCS5Padding - Padding, jedyny, który działał pomiędzy płytką, a aplikacją.

Klucz (hasło) szyfrowania i deszyfrowania jest następujący:
```cpp
KEY = {
	0x2B, 0x7E, 0x15, 0x16,
	0x28, 0xAE, 0xD2, 0xA6,
	0xAB, 0xF7, 0x15, 0x88,
	0x09, 0xCF, 0x4F, 0x3C
}
```

A wektor inicjalizujący algorytm `AES` wygląda tak:
```cpp
IV = {
	0xAA, 0xAA, 0xAA, 0xAA,
	0xAA, 0xAA, 0xAA, 0xAA,
	0xAA, 0xAA, 0xAA, 0xAA,
	0xAA, 0xAA, 0xAA, 0xAA
}
```

Proces szyfrowania i deszyfrowania jest banalnie prosty:

[INPUT (PLAIN TEXT)] -> [ENCRYPT (BINARY)] -> [ENCODE (BASE64)] -> [SEND (BLUETOOTH)] -> [RECIVE (BLUETOOTH)] -> [DECODE (BASE64)] -> [DECRYPT (BINARY)] -> [OUTPUT (PLAIN TEXT)]

W skrócie, szyfrowanie i deszyfrowanie wygląda tak:

```kotlin
fun encrypt(data: String): String {
	val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")			//	Get correct cipher
	val secretKey = SecretKeySpec(key, "AES")						//	Pass password
	val ivParameterSpec = IvParameterSpec(iv)						//	Pass IV

	cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec)	//	Init cipher
	val decryptedData = cipher.doFinal(data.toByteArray())			//	Encrypt data to binary (not human readable)

	return String(Base64.getEncoder().encode(decryptedData))		//	Encode binary to Base64 (human readable)
}
```

```kotlin
fun decrypt(data: String): String {
	val decodedData = Base64.getDecoder().decode(data)              //	Decode Base64 to binary (not human readable)

	val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")    		//	Get correct cipher
	val secretKey = SecretKeySpec(key, "AES")                      	//	Pass password
	val ivParameterSpec = IvParameterSpec(iv)                       //	Pass IV

	cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec)    //	Init cipher
	val decryptedData = cipher.doFinal(decodedData)                 //	Decrypt data to plain text (human readable)

	return String(decryptedData)
}
```
