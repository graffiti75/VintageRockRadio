1) Create an Android app that uses a Youtube API to get Youtube videos by their ids.

2) This app must have only one screen.

3) Into this screen we can play those Youtube videos in a fancy Jetpack Compose UI that simulates a 70's music vintage radio hi-res player with a play/pause button, a slider to show the music status in seconds, and a video player to show the Youtube video.

4) This Youtube Video Player must be placed in the top/left part of this UI.

5) This app must use Jetpack Compose with MVI architecture: State classes, ViewModel and onAction classes, like for instance is showed below:

### LoginScreenState.kt

```
data class LoginScreenState(
	val loading : Boolean = true,
	val alert : MessageAlert? = null,
	val user : String = "",
	val userLogged : Boolean = false
)
```

### LoginScreenAction.kt

```
sealed interface LoginScreenAction {
	data class OnLoginClick(val email: String, val password: String) : LoginScreenAction
	data class OnCreateUser(val email: String, val password: String) : LoginScreenAction
	data object OnDismissAlert : LoginScreenAction
}
```

### LoginScreenViewModel.kt

```
@HiltViewModel
class LoginScreenViewModel @Inject constructor(
	private val auth: UserAuthentication
): ViewModel() {

	private val _state = MutableStateFlow(LoginScreenState())
	val state: StateFlow<LoginScreenState> = _state.asStateFlow()

	private val _events = Channel<UiEvent>()
	val events = _events.receiveAsFlow()

	fun onAction(action: LoginScreenAction) {
		when (action) {
			is LoginScreenAction.OnLoginClick -> login(action.email, action.password)
			is LoginScreenAction.OnCreateUser -> createUser(action.email, action.password)
			is LoginScreenAction.OnDismissAlert -> dismissAlert()
		}
	}
	...
}
```

### LoginScreenRoot.kt

```
@Composable
fun LoginScreenRoot(
	modifier: Modifier = Modifier,
	viewModel: LoginScreenViewModel = hiltViewModel()
) {
	val state by viewModel.state.collectAsStateWithLifecycle()
	val onAction = viewModel::onAction

	if (state.loading) {
		LoadingScreen()
	} else {
		Box(
			contentAlignment = Alignment.BottomCenter,
			modifier = Modifier.fillMaxSize()
		) {
			LoginScreen(
				modifier = modifier,
				onAction = onAction,
				state = state
			)
			state.alert?.let {
				BottomAlert(
					onDismiss = { onAction(LoginScreenAction.OnDismissAlert) },
					alert = it
				)
			}
		}
	}
}

@Composable
private fun LoginScreen(
	modifier: Modifier = Modifier,
	onAction: (LoginScreenAction) -> Unit,
	state: LoginScreenState
) {
	...
	Button(
			onClick = {
				keyboardController?.hide()
				onAction(LoginScreenAction.OnCreateUser(email, password)) // Example of click inside the app
			},
			modifier = Modifier.fillMaxWidth()
		) {
			Text("Sign Up")
		}
	}
	...
}
```

6) I don't want you to create a Login app. I just used the examples above to show you the behavior I want into this app.

7) I want this app to place all Gradle dependencies inside a libs.versions.toml file.

8) This app must run in Landscape mode.

9) This app's source of data (containing Youtube IDs) must be placed in a file called ids.txt. This file, initially, must contain the following infos: Decade;Year;Band;Song;YoutubeID.

```
70;1975;10cc;I'm Not In Love;OtBHfxU2wmc
70;1979;ACDC;Highway To Hell;l482T0yNkeo
70;1976;ACDC;It's A Long Way To The Top;-sUXMzkh-jI
70;1974;ACDC;Jailbreak;WHtWs4wiFCs
70;1977;ACDC;Whole Lotta Rosie;QMvE0yFnR0I
70;1973;Aerosmith;Dream On;9bAoq7k3tZ0
70;1975;Aerosmith;Sweet Emotion;82cJgPXU-ik
70;1978;Alice Cooper;How You Gonna See Me Now;X7jYw4VZC0c
70;1976;Alice Cooper;I Never Cry;3AUkUfH29yA
70;1972;Alice Cooper;School's Out;f0Q9en-GEBU
70;1977;Alice Cooper;You And Me;TyYhd0H9bLI
70;1971;America;A Horse With No Name;zSAJ0l4OBHM
70;1973;Bachman Turner Overdrive;Takin' Care Of Business;NCIUf8eYPqA
70;1974;Bachman Turner Overdrive;You Ain't Seen Nothin' Yet;4cia_v4vxfE
70;1974;Bad Company;Bad Company;JXQJpyQBShU
70;1974;Bad Company;Can't Get Enough;4XwKk_LmwTI
70;1970;Barclay James Harvest;The Iron Maiden;hhxm8fJECCE
70;1971;Black Sabbath;After Forever;UiwjyeR0drI
70;1972;Black Sabbath;Changes;nPtorZ2k7Ak
70;1971;Black Sabbath;Children Of The Grave;X7UZeHvMYZA
70;1970;Black Sabbath;Evil Woman;IE8lXuSDVNU
70;1978;Black Sabbath;Hard Road;oZnRhqZuJjs
70;1975;Black Sabbath;Hole In The Sky;ruhxGENigrk
70;1971;Black Sabbath;Into The Void;R5XnrZn47Q
70;1970;Black Sabbath;Iron Man;8aQRq9hhekA
70;1970;Black Sabbath;N.I.B.;RaRtXYSszsA
70;1978;Black Sabbath;Never Say Die;D3-hNMqmKK0
70;1970;Black Sabbath;Paranoid;uk_wUT1CvWM
70;1973;Black Sabbath;Sabbath Bloody Sabbath;mfTpjrzas5E
70;1974;Black Sabbath;Sabbra Cadabra;noUYAPdLMp0
70;1970;Black Sabbath;Sleeping Village;5unr2J_XhLI
70;1972;Black Sabbath;Snowblind;crDTz1skj9c
70;1971;Black Sabbath;Solitude;7WKjNOU72Co
70;1972;Black Sabbath;Supernaut;MrEHqbkcJvA
70;1971;Black Sabbath;Sweet Leaf;W-zmtmgswHw
70;1975;Black Sabbath;Symptom Of The Universe;4qDYa2aIBxw
70;1970;Black Sabbath;The Wizard;MeZ8uVIOIhM
70;1970;Black Sabbath;War Pigs;LQUXuQ6Zd9w
70;1978;Blondie;Heart Of Glass;WGU_4-5RaxU
70;1978;Blondie;One Way Or Another;_zBwRDEFMRY
70;1976;Blue Oyster Cult;(Don't Fear) The Reaper;Dy4HA3vUv2c
70;1974;Bob Dylan;Knockin' On Heaven's Door;rnKbImRPhTE
70;1975;Bob Dylan;Tangled Up In Blue;YwSZvHqf9qM
70;1978;Bob Seger & Silver Bullet Band;Old Time Rock 'n' Roll;SoaAb5MnKtY
70;1976;Bob Seger;Night Moves;_mRFWQoXq4c
70;1973;Bob Seger;Turn The Page;3khH9ih2XJg
70;1978;Bonnie Tyler;It's A Heartache;nPOy7TPjfkE
70;1976;Boston;Long Time;TnwqUEelQjE
70;1976;Boston;More Than A Feeling;GSism86Y4bk
70;1972;Bread;The Guitar Man;CpOjQvADLG4
70;1975;Bruce Springsteen;Thunder Road;JGBXnw86Mgc
70;1972;Carly Simon;You're So Vain;mQZmCJUSC6g
70;1971;Carole King;It's Too Late;VkKxmnrRVHo
70;1971;Carpenters;Superstar;SJmmaIGiGBg
70;1973;Carpenters;This Masquerade;5GChYjK8rIk
70;1970;Cat Stevens;Wild World;rBA1jocMvnc
70;1978;Cheap Trick;Surrender;Av7xWys6gh4
70;1976;Chicago;If You Leave Me Now;cYTmfieE8jI
70;1970;Creedence Clearwater Revival;Have You Ever Seen The Rain;Gu2pVPWGYMQ
70;1970;Creedence Clearwater Revival;Who'll Stop The Rain;lIPan-rEQJA
70;1970;Creedence Clearwater Revival;Wish I Could Hideaway;7R_NpTWxlqg
70;1970;Crosby, Stills, Nash & Young;4 + 20;9ZHzPLJq4DA
70;1970;Crosby, Stills, Nash & Young;Almost Cut My Hair;4Lk2KHajp4Y
70;1970;Crosby, Stills, Nash & Young;Carry On;nP0VBB7BO64
70;1970;Crosby, Stills, Nash & Young;Country Girl;C7fp3Ui5bTQ
70;1970;Crosby, Stills, Nash & Young;Deja Vu;YCs6Tpd5sFQ
70;1971;Crosby, Stills, Nash & Young;Find The Cost Of Freedom;GMfvYxK9Zoo
70;1970;Crosby, Stills, Nash & Young;Helpless;C8LYOyqJE7k
70;1971;Crosby, Stills, Nash & Young;Ohio;l1PrUU2S_iw
70;1970;Crosby, Stills, Nash & Young;Our House;tKYjUn-SBcg
70;1970;Crosby, Stills, Nash & Young;Woodstock;TrWNTqbLFFE
70;1972;Curved Air;Melinda More Or Less;t3aSY0MNvTs
70;1970;David Bowie;After All;OXewmtDGQFQ
70;1970;David Bowie;Black Country Rock;JqwXso5d1vo
70;1971;David Bowie;Changes;pl3vxEudif8
70;1973;David Bowie;Drive-In Saturday;WABWNOEwC9A
70;1975;David Bowie;Fame;J-_30HA7rec
70;1976;David Bowie;Heroes;xEqSOst1dg8
70;1972;David Bowie;It Ain't Easy;OnI_ko3_r_c
70;1973;David Bowie;Lady Grinning Soul;6fHoMw8tCzo
70;1972;David Bowie;Lady Stardust;5UQvBzo_rJA
70;1972;David Bowie;Moonage Daydream;JFDj3shXvco
70;1974;David Bowie;Rebel Rebel;U16Xg_rQZkA
70;1972;David Bowie;Rock 'n' Roll Suicide;9jg4ekLG9Zo
70;1970;David Bowie;Running Gun Blues;WM74wALZJjk
70;1970;David Bowie;She Shook Me Cold;u08RG6HYaO4
70;1972;David Bowie;Starman;tRcPA7Fzebw
70;1972;David Bowie;Suffragette City;CEkXAHIKdKI
70;1974;David Bowie;Sweet Thing;wXXZVh3882c
70;1973;David Bowie;The Jean Genie;kMYg_Ra4cr8
70;1970;David Bowie;The Man Who Sold The World;g33-W9t2q2Q
70;1973;David Bowie;The Prettiest Star;QFlsm3zqSv8
70;1970;David Bowie;The Width Of A Circle;s2L4hL2IvUk
70;1973;David Bowie;Watch That Man;G1loH-YvTDY
70;1972;David Bowie;Ziggy Stardust;XXq5VvYAI1Q
70;1971;Deep Purple;Anyone's Daughter;eCAMOY9Vut8
70;1970;Deep Purple;Black Night;QuAKMlfxX7I
70;1970;Deep Purple;Bloodsucker;6LGtYLpUs4w
70;1974;Deep Purple;Burn;LCnebZnysmI
70;1970;Deep Purple;Child In Time;PfAWReBmxEs
70;1970;Deep Purple;Cry Free;UuMHFlls6lE
70;1971;Deep Purple;Demon's Eye;kDNnARSBamU
70;1971;Deep Purple;Fireball;lFHTWdQkqYw
70;1970;Deep Purple;Flight Of The Rat;XxNYOcNnmGI
70;1971;Deep Purple;Fools;VuArDaJ0I6Y
70;1971;Deep Purple;Freedom;pyenXwFKtlU
70;1970;Deep Purple;Hard Loving Man;93pE3npDRRs
70;1972;Deep Purple;Highway Star;Wr9ie2J2690
70;1970;Deep Purple;Into The Fire;L97BScF-5dE
70;1974;Deep Purple;Lady Double Dealer;7LLBwzMxgQA
70;1972;Deep Purple;Lazy;dX5JEsKWFW4
70;1970;Deep Purple;Living Wreck;pizC-Cstgec
70;1972;Deep Purple;Maybe I'm A Leo;7PpjboCizFI
70;1974;Deep Purple;Mistreated;JyAZ4oEQkTs
70;1972;Deep Purple;Never Before;6vV96vkU7XU
70;1971;Deep Purple;No No No;DjZONFu1fps
70;1971;Deep Purple;No One Came;luVXf9UpCiM
70;1973;Deep Purple;Our Lady;rmAvlyV_c4s
70;1972;Deep Purple;Pictures Of Home;-qMnp8mQ0JQ
70;1973;Deep Purple;Place In Line;mFwrKtoleRs
70;1973;Deep Purple;Rat Bat Blue;6poRsrl_574
70;1972;Deep Purple;Smoke On The Water;zUwEIt9ez7M
70;1973;Deep Purple;Smooth Dancer;gAHIgvIFggo
70;1974;Deep Purple;Soldier Of Fortune;xVO7sQRBcuQ
70;1972;Deep Purple;Space Truckin';hHOrpFeXUao
70;1970;Deep Purple;Speed King;W_jfHvcAXRY
70;1974;Deep Purple;Stormbringer;4C2K889u_90
70;1971;Deep Purple;Strange Kind Of Woman;bAzjVdD06z8
70;1974;Deep Purple;The Gypsy;Pjy9MUjrjdI
70;1971;Deep Purple;The Mule;512UKHYD1gk
70;1972;Deep Purple;When I Blind Man Cries;9_Iq9CWuqMM
70;1973;Deep Purple;Woman From Tokyo;T7SFjtxL_qM
70;1970;Derek And The Dominos;Layla;uSquiIVLhrQ
70;1978;Dire Straits;Sultans Of Swing;0fAQhSRLQnM
70;1971;Don McLean;American Pie;uAsV5-Hv-7U
70;1976;Eagles;Hotel California;EqPtz5qN7HM
70;1979;Electric Light Orchestra;Last Train To London;Up4WjdabA2c
70;1976;Electric Light Orchestra;Livin' Thing;H48j3KGBomU
70;1970;Elton John;Love Song;FFRFdx-RjLw
70;1973;Elton John;Bennie And The Jets;p5rQHoaQpTw
70;1973;Elton John;Crocodile Rock;QS-YZlJLCbM
70;1973;Elton John;Daniel;UA78e27R_J4
70;1973;Elton John;Goodbye Yellow Brick Road;DDOL7iY8kfo
70;1972;Elton John;Rocket Man;DtVBCG6ThDk
70;1973;Elton John;Saturday Night's Alright For Fighting;26wEWSUUsUc
70;1975;Elton John;Someone Saved My Life Tonight;VZRRd4bW91c
70;1971;Elton John;Tiny Dancer;yYcyacLRPNs
70;1975;Elton John;We All Fall In Love Sometimes;OoeitEIfAls
70;1971;Elton John;Your Song;GlPlfCy1urI
70;1973;Elvis Presley;Burning Love;zf2VYAtqRe0
70;1974;Elvis Presley;My Boy;HOLH63c7SG0
70;1971;Emerson, Lake & Palmer;A Time And A Place;diqXzi39tsA
70;1973;Emerson, Lake & Palmer;Benny The Bouncer;zFzr_MFX0Ok
70;1977;Emerson, Lake & Palmer;C'est La Vie;BvNJ1RpYjsI
70;1977;Emerson, Lake & Palmer;Food For Your Soul;_L-2SXD9RI0
70;1972;Emerson, Lake & Palmer;From The Beginning;47S-lRwoK7c
70;1977;Emerson, Lake & Palmer;Hallowed Be Thy Name;TmAZ-mNEthk
70;1977;Emerson, Lake & Palmer;I Believe In Father Christmas;uMtteocAA80
70;1971;Emerson, Lake & Palmer;Infinite Space (Conclusion);ZBYFB8VauAM
70;1971;Emerson, Lake & Palmer;Jeremy Bender;AIT2rsoC22E
70;1973;Emerson, Lake & Palmer;Karn Evil 9;fLS0Med0s6E
70;1970;Emerson, Lake & Palmer;Knife Edge;XwIehM-x6kA
70;1977;Emerson, Lake & Palmer;L.A. Nights;yemZPocWUA8
70;1970;Emerson, Lake & Palmer;Lucky Man;89g1P_J40JA
70;1977;Emerson, Lake & Palmer;New Orleans;48FtqyrC6pY
70;1972;Emerson, Lake & Palmer;Nutrocker;CiRBQ_hSNt0
70;1973;Emerson, Lake & Palmer;Still... You Turn Me On;McNQqH3RGGI
70;1970;Emerson, Lake & Palmer;Take A Pebble;SrYbfQRlfwQ
70;1972;Emerson, Lake & Palmer;The Gnome;-mTR8Z8aR2Y
70;1972;Emerson, Lake & Palmer;The Sage;5AXxQGai4JU
70;1970;Eric Clapton;After Midnight;AvxJ0TVvVzE
70;1977;Eric Clapton;Cocaine;3bEUaeG4wH4
70;1977;Eric Clapton;Wonderful Tonight;fxAiUq8yn34
70;1977;Fleetwood Mac;Dreams;mrZRURcb1cM
70;1977;Fleetwood Mac;Go Your Own Way;6ul-cZyuYq4
70;1975;Fleetwood Mac;Rhiannon;U_aYibUx1B8
70;1975;Foghat;Slow Ride;GcCNcgoyG_0
70;1978;Foreigner;Double Vision;oxKCPjcvbys
70;1970;Free;All Right Now;lSdBtoIIYT4
70;1972;Gary Glitter;Rock and Roll, Part 2;t-hB1TzoG7M
70;1974;Genesis;Carpet Crawlers;2yUN6CsuVPw
70;1978;Genesis;Follow You Follow Me;DyDRXbP1MaY
70;1970;George Harrison;My Sweet Lord;8qJTJNfzvr8
70;1970;Grand Funk Railroad;I'm Your Captain;FrlE3JOQ7bE
70;1973;Grand Funk Railroad;We're An American Band;Zc_JcGuH5Z8
70;1977;Heart;Barracuda;PeMvMNpvB5M
70;1977;Iggy Pop;Lust For Life;jQvUBf5l7Vw
70;1972;James Brown;I Got Ants In My Pants (And I Want To Dance);fB-hpewwEiY
70;1971;James Brown;Sex Machine;1UzZUfFUnxY
70;1970;James Taylor;Fire And Rain;C3uaXCJcRrE
70;1977;James Taylor;Handy Man;dXI43zGeyu4
70;1971;James Taylor;You've Got A Friend;xEkIou3WFnM
70;1970;Janis Joplin;A Woman Left Lonely;klhK_4evO5c
70;1970;Janis Joplin;Me And Bobby McGee;WXV_QjenbDw
70;1970;Janis Joplin;Move Over;eihw2hu65S0
70;1971;Jethro Tull;Aqualung;B0jMPI_pUec
70;1974;Jethro Tull;Back Door Angels;9ZwDnoTTJoU
70;1978;Jethro Tull;Heavy Horses;vRHATZzMh-g
70;1977;Jethro Tull;Jack In The Green;9fWzUgrYnqs
70;1971;Jethro Tull;Locomotive Breath;i19d1QnstsA
70;1970;Jethro Tull;Nothing To Say;b1Pzk_UYnos
70;1977;Jethro Tull;Songs From The Wood;z4UYX2qpUK0
70;1972;Jethro Tull;Thick as a Brick (Pt. 1);ldXdnZtTWp8
70;1972;Jethro Tull;Thick as a Brick (Pt. 2);GTWQv8RsI6s
70;1970;Jethro Tull;To Cry You A Song;S5vto70Q23E
70;1977;Jethro Tull;Velvet Green;6-ANplhDJNY
70;1977;Jethro Tull;Velvet Green;JG77YHX5yYE
70;1970;Jethro Tull;With You There to Help Me;gfKzPV-Ely4
70;1971;Jethro Tull;Wond'ring Aloud;luDfuZkeqKU
70;1971;John Lennon;Imagine;YkgkThdzX-8
70;1970;John Lennon;Instant Karma;xLy2SaSQAtA
70;1970;Jupiter Sunset;Back In The Sun;PuM_sovJxGU
70;1976;Kansas;Carry On Wayward Son;2X_2IdybTV0
70;1977;Kansas;Dust In The Wind;tH2w6Oxx0kQ
70;1978;Kate Bush;Wuthering Heights;-1pMMIe4hb4
70;1979;Kiss;2000 Man;rip_CoRVZto
70;1976;Kiss;Detroit Rock City;iZq3i94mSsQ
70;1979;Kiss;I Was Made For Lovin' You;12fJAnaif34
70;1977;Kiss;Love Gun;trR5ROuf1Uk
70;1975;Kiss;Rock And Roll All Nite;EFMD7Usflbg
70;1979;Kiss;Sure Know Something;PSURhxNYmDw
70;1974;Kraftwerk;Autobahn;gChOifUJZMc
70;1978;Kraftwerk;The Model;KFq2pU21cNU
70;1976;Led Zeppelin;Achilles Last Stand;1t4KLOm7pO0
70;1979;Led Zeppelin;All Of My Love;z0DAnu5Sq6k
70;1975;Led Zeppelin;Black Country Woman;1cVuETI4Pl0
70;1971;Led Zeppelin;Black Dog;fl6s1x9j4QQ
70;1975;Led Zeppelin;Boogie With Stu;REAP66zFhxU
70;1971;Led Zeppelin;Bron-Y-Aur Stomp;oC-9aEf0Q-A
70;1975;Led Zeppelin;Bron-Yr-Aur;QKge6Ay9O4E
70;1971;Led Zeppelin;Celebration Day;KMQdiHIZgSo
70;1975;Led Zeppelin;Custard Pie;hB43v5rWBF0
70;1973;Led Zeppelin;D'yer Mak'er;_pFZz3OXcMs
70;1973;Led Zeppelin;Dancing Days;qztKD75J2BM
70;1975;Led Zeppelin;Down By The Seaside;pvGxEP4aF9E
70;1979;Led Zeppelin;Fool In The Rain;Zp-LBD_q0sQ
70;1976;Led Zeppelin;For Your Life;zkFh7fC7-h8
70;1971;Led Zeppelin;Four Sticks;iJp27QMR2KU
70;1971;Led Zeppelin;Gallows Pole;RSZca1Q9IWA
70;1971;Led Zeppelin;Going To California;7IZ-jATBq9A
70;1970;Led Zeppelin;Hey Hey What Can I Do;epX8Th4aiMc
70;1975;Led Zeppelin;Houses Of The Holy;ohDQ1FUUjPs
70;1976;Led Zeppelin;I'm Gonna Crawl;bVPDP_DEsJs
70;1971;Led Zeppelin;Immigrant Song;y8OtzJtp-EM
70;1975;Led Zeppelin;In My Time Of Dying;kTdvekG949c
70;1979;Led Zeppelin;In The Evening;pqzEDWFWWLQ
70;1975;Led Zeppelin;In The Light;pNo1nS_JV5k
70;1975;Led Zeppelin;Kashmir;3W6mDUmPZ0Y
70;1971;Led Zeppelin;Misty Mountain Hop;n6fBQRaygeo
70;1975;Led Zeppelin;Night Flight;CrFh_P0yaS0
70;1973;Led Zeppelin;No Quarter;wdqDmJbm2eY
70;1976;Led Zeppelin;Nobody's Fault But Mine;la-zf2TgCjw
70;1970;Led Zeppelin;Out On The Tiles;_3ioOxQ76dA
70;1973;Led Zeppelin;Over The Hills And Far Away;Ee33FsDANk0
70;1971;Led Zeppelin;Rock And Roll;lncr2g9XJHU
70;1976;Led Zeppelin;Royal Orleans;hXCmPHgg34o
70;1975;Led Zeppelin;Sick Again;S8KDXntYwGQ
70;1970;Led Zeppelin;Since I've Been Loving You;K8R7zjJMIfU
70;1971;Led Zeppelin;Stairway To Heaven;iXQUu5Dti4g
70;1970;Led Zeppelin;Tangerine;_0Auvlsv860
70;1976;Led Zeppelin;Tea For One;_I_uLwq3iwI
70;1975;Led Zeppelin;Ten Years Gone;kWbO9a1_Z3U
70;1970;Led Zeppelin;That's The Way;GGsmyqIrZRo
70;1971;Led Zeppelin;The Battle Of Evermore;88b0OYxdtyM
70;1973;Led Zeppelin;The Ocean;H8bVaTW6UCU
70;1973;Led Zeppelin;The Rain Song;TRt4hQs3nH0
70;1975;Led Zeppelin;The Rover;qK_ZtN9K2EM
70;1973;Led Zeppelin;The Song Remains The Same;dRnKvXqti6M
70;1975;Led Zeppelin;The Wanton Song;KTypf_JMFis
70;1975;Led Zeppelin;Trampled Under Foot;ftknR1gf9qw
70;1971;Led Zeppelin;When The Levee Breaks;28ZOwVAZS0o
70;1973;Lobo;How Can I Tell Her;-aTK1n1yvUo
70;1972;Lobo;I'd Love You To Want Me;fbP6RPdg7qU
70;1972;Lou Reed;Walk On The Wild Side;oG6fayQBm9w
70;1975;Lynyrd Skynyrd;Saturday Night Special;3vF66CsYEnc
70;1973;Lynyrd Skynyrd;Sweet Home Alabama;SbmYRZXPvhI
70;1970;Marmalade;Reflections Of My Life;zqUDRkO93DA
70;1977;Meat Loaf;Paradise By The Dashboard Light;C11MzbEcHlw
70;1974;Montrose;I Got The Fire;ccQRJJoW7TY
70;1979;Motorhead;Overkill;3VNUyjRRjxM
70;1979;Motorhead;Stay Clean;J8MuArrlmmw
70;1974;Mott The Hoople;All The Young Dudes;yNHdPPJGowY
70;1970;Mountain;Theme for An Imaginary Western;0l_x0xH9fLM
70;1970;Mungo Jerry;In The Summertime;wvUQcnfwUUM
70;1975;Nazareth;Beggar's Day;gNJtMK5E0nQ
70;1973;Nazareth;Broken Down Angel;zboXNSXqOtE
70;1975;Nazareth;Changin' Times;HG3eahyw5os
70;1977;Nazareth;Expect No Mercy;BwMxpNhBAmk
70;1975;Nazareth;Hair Of The Dog;kyXz6eMCj2k
70;1976;Nazareth;Holly Roller;NRJUr-iP-8o
70;1976;Nazareth;I Don't Want To Go On Without You;IZtYVpK4s7k
70;1976;Nazareth;L.A. Girls;BVPFI6YOYVE
70;1975;Nazareth;Love Hurts;qn-z5pIe5PQ
70;1975;Nazareth;Miss Misery;UTFM9MmD_Mk
70;1973;Nazareth;Not Faking It;UJDnah3OYu0
70;1977;Nazareth;Place In Your Heart;jUX4BT492Bs
70;1973;Nazareth;Razamanaz;fpeI5Z4vO-4
70;1977;Nazareth;Revenge Is Sweet;nUKe97AefOM
70;1974;Nazareth;Shapes Of Things;HOC9mn7l9c4
70;1977;Nazareth;Shot Me Down;qGzTb7d1iX0
70;1978;Nazareth;Star;SXkd1zKZNp0
70;1974;Nazareth;Sunshine;TRNL3QOLT2Q
70;1971;Nazareth;The King Is Dead;sn3MWS_fnBw
70;1973;Nazareth;This Flight Tonight;ylW6sC6NNhY
70;1973;Nazareth;Turn On Your Receiver;KJj--xPPGHw
70;1971;Nazareth;Witchdoctor Woman;CvqzafzEfdU
70;1972;Neil Young;Heart Of Gold;X3IA6pIVank
70;1979;Neil Young;Hey Hey My My (Into The Black);331kyZ9OXMc
70;1978;Patti Smith;Because The Night;c_BcivBprM0
70;1973;Paul McCartney & The Wings;Band On The Run;RjlvdcBAKdg
70;1973;Paul McCartney & The Wings;Live And Let Die;e7aGAIWe3uE
70;1976;Paul McCartney & The Wings;Silly Love Songs;wh15LOppcWQ
70;1970;Paul McCartney;Maybe I'm Amazed;DNh5Ca1dIXM
70;1979;Pink Floyd;Another Brick In The Wall, Part 2;x71uD9ybrzk
70;1973;Pink Floyd;Breath;mrojrDCI02k
70;1979;Pink Floyd;Comfortably Numb;_FrOQC-zEog
70;1970;Pink Floyd;Fat Old Sun;PcT-xjnHCLA
70;1973;Pink Floyd;Money;JkhX5W7JoWI
70;1973;Pink Floyd;Time;JwYX52BP2Sk
70;1975;Pink Floyd;Wish You Were Here;IXdNnw99-Ic
70;1977;Player;Baby Come Back;Hn-enjcgV1o
70;1975;Queen;'39;kE8kGMfXaFU
70;1975;Queen;Bohemian Rhapsody;fJ9rUzIMcZQ
70;1975;Queen;Death On Two Legs;kqVpk0qxmfA
70;1973;Queen;Great King Rat;dGAioru4U0s
70;1975;Queen;I'm Love With My Car;oaEM4JYFPfw
70;1973;Queen;Keep Youself Alive;5VmEXWpvfhc
70;1974;Queen;Killer Queen;2ZBtPf7FOoM
70;1978;Queen;Let Me Entertain You;LmeOC0OTY_Q
70;1974;Queen;Loser In The End;-IzXzHtvgE8
70;1975;Queen;Love Of My Life;T73WhWTawCE
70;1973;Queen;Modern Times Rock N' Roll;ohjGsR4HIs0
70;1974;Queen;Now I'm Here;Unoab2tgJ3I
70;1974;Queen;Seven Seas Of Rhye;FxIo57WURRE
70;1977;Queen;Sheer Heart Attack;rkHF_JMnB8o
70;1976;Queen;Somebody To Love;kijpcUv-b8M
70;1974;Queen;Stone Cold Crazy;VZRRd4bW91c
70;1974;Queen;Tenement Funster;0E2mjJvBKs8
70;1976;Queen;Tie Your Mother Down;LvB2MnIIdMw
70;1977;Queen;We Are The Champions;hSTivVclQQ0
70;1977;Queen;We Will Rock You;-tJYN-eG1zk
70;1974;Queen;White Queen;Nx_SVPiXnWM
70;1975;Queen;You Are My Best Friend;HaZpZQG2z10
70;1979;Rainbow;All Night Long;ju1TrKuQGMg
70;1975;Rainbow;Black Sheep Of The Family;WsW3654eUxE
70;1975;Rainbow;Catch The Rainbow;v5J2yjSmt_w
70;1979;Rainbow;Danger Zone;EeVFTeXs1o8
70;1979;Rainbow;Eyes Of The World;ufPPB0SMn-g
70;1978;Rainbow;Gates Of Babylon;SH43BU6DSm0
70;1978;Rainbow;Kill The King;2YYP7LlLsbA
70;1978;Rainbow;Lady Of The Lake;OUodkI3VsTw
70;1978;Rainbow;Long Live Rock 'N' Roll;wGkNMy2P2gY
70;1979;Rainbow;Lost In Hollywood;mRLHHftZEJA
70;1979;Rainbow;Love's No Friend;Me661ZymqRg
70;1975;Rainbow;Man On The Silver Mountain;RKSJNnbHiOQ
70;1975;Rainbow;Self Portrait;RriUpGA5Ang
70;1975;Rainbow;Sixteenth Century Greensleeves;hAbWj4JL22c
70;1975;Rainbow;Snake Charmer;Pq35nvahbR4
70;1976;Rainbow;Stargazer;W6CjO0H2j0s
70;1976;Rainbow;Starstruck;6FWdy6H_jcE
70;1976;Rainbow;Tarot Woman;AhdmFx2kQAg
70;1975;Rainbow;The Temple Of The King;ZUpxniZBb5s
70;1977;Ram Jam;Black Betty;I_2D8Eo15wE
70;1972;Raspberries;Don't Want To Say Goodbye;WJsf1Yp5blw
70;1971;Rod Stewart;Maggie May;EOl7dh7a-6g
70;1976;Rod Stewart;Tonight's The Night;IZr6AE-u2UM
70;1975;Roxy Music;Love Is The Drug;0n3OepDn5GU
70;1975;Rush;Anthem;_VcswhBy9tk
70;1970;Santana;Black Magic Woman;wyQUCYl-ocs
70;1976;Scorpions;Backstage Queen;zIAQSzmZW_s
70;1977;Scorpions;Born To Touch Your Feelings;Tua0viBBQxM
70;1976;Scorpions;Catch Your Train;99wvuG-U61U
70;1975;Scorpions;Dark Lady;qdBOnTjTpoI
70;1974;Scorpions;Drifting Sun;C85caMVcJh4
70;1974;Scorpions;Far Away;VxWWJU7K92w
70;1973;Scorpions;Fly People Fly;bhbgbz4xeCw
70;1974;Scorpions;Fly To The Rainbow;dGDefpjTqFY
70;1977;Scorpions;He's A Woman, She's A Man;xlhIpw-07Xc
70;1979;Scorpions;Holiday;IBtGmxU1wzs
70;1977;Scorpions;I've Got To Be Free;N1Pe6cq--GE
70;1974;Scorpions;In Trance;JaJPxkby8AU
70;1976;Scorpions;In Your Park;Q-wCrI01KlA
70;1975;Scorpions;Life's Like A River;5SbVUlGcfc0
70;1975;Scorpions;Living And Dying;v7M09lcU51U
70;1975;Scorpions;Night Lights;sZWC2flbtHM
70;1976;Scorpions;Picture Life;cmUHo2fnupo
70;1977;Scorpions;Polar Nights (Live);fG_ysfc50nU
70;1976;Scorpions;Polar Nights;ah3kVWtoM1E
70;1975;Scorpions;Robot Man;EohxqqKaYFc
70;1974;Scorpions;Speedy's Coming;YUO7wYRmpnk
70;1977;Scorpions;Steamrock Fever;X1oz3_o5w7M
70;1975;Scorpions;Sun In My Hand;IrfuhTU-U2A
70;1977;Scorpions;Suspender Love;cMJ1U0O_Y8Q
70;1977;Scorpions;The Sails Of Charon;KJe_ZWsgi8s
70;1974;Scorpions;They Need A Million;qQo90IISQAM
70;1974;Scorpions;This Is My Song;G31aSs2flbg
70;1976;Scorpions;Virgin Killer;r1r6Bas1SXw
70;1977;Scorpions;We'll Burn The Sky;3406cKRen7Y
70;1976;Scorpions;Yellow Raven;ESO0rpDtud8
70;1977;Scorpions;Your Light;tflAb0YjAQI
70;1970;Simon & Garfunkel;Bridge Over Troubled Water;4G-YQA_bsOU
70;1973;Slade;Cum On Feel The Noize;Qu_ozjAu_vM
70;1970;Stephen Stills;Black Queen;tee61YGheaA
70;1970;Stephen Stills;Do For The Others;-OSaIUKi8p4
70;1970;Stephen Stills;Go Back Home;Pd9VNlquFlE
70;1970;Stephen Stills;Old Times Good Times;z4DJ3pnVvOw
70;1976;Steve Miller Band;Fly Like An Eagle;c1f7eZ8cHpM
70;1976;Steve Miller Band;Rock'n' Me;xTwBL7V9SwE
70;1972;Stevie Wonder;Superstition;0CFuCYNx-1g
70;1979;Supertramp;Breakfast In America;PZahtmWhH5g
70;1979;Supertramp;The Logical Song;ukKQw578Lm8
70;1978;Suzi Quatro;If You Can't Give Me Love;SeI42SvFy7Y
70;1979;Suzi Quatro;She's In Love With You;BZK28ue0Pc8
70;1972;Sweet;Blockbuster;Y64211sjSko
70;1974;Sweet;Fox On The Run;qBdFA6sI6-8
70;1973;Sweet;The Ballroom Blitz;mPQPdYttl7U
70;1971;T. Rex;Bang A Gong (Get It On);TVEhDrJzM8E
70;1973;The Allman Brothers;Ramblin' Man;7KeoYzHPKF0
70;1970;The Beatles;I’ve Got A Feeling;DbKPZd5oihc
70;1970;The Beatles;Let It Be;QDYfEBY9NM4
70;1970;The Beatles;One After 909;t8UeWjynWvE
70;1970;The Beatles;The Long And Winding Road;fR4HjTH_fTM
70;1970;The Beatles;Two Of Us;cLQox8e9688
70;1979;The Clash;London Calling;EfK-WX2pa8c
70;1979;The Clash;Train In Vain;aUzBgeI5dpc
70;1971;The Doors;L.A. Woman;JskztPPSJwY
70;1971;The Doors;Riders On The Storm;iv8GW1GaoIc
70;1970;The Doors;Roadhouse Blues;n2_X4VTCoEo
70;1976;The Eagles;Life In The Fast Lane;4tcXblWojdM
70;1975;The Eagles;One Of These Nights;ESc2Tq2HzhQ
70;1972;The Eagles;Take It Easy;LfeNhwnO8hw
70;1973;The Government;Bang Bang;m2vqE6M_ea8
70;1970;The Grateful Dead;Truckin';pafY6sZt0FE
70;1974;The Hollies;Don't Let Me Down;vtIVJJsTxxo
70;1971;The Hollies;Long Cool Woman;lP94PlEtsEQ
70;1974;The Hollies;The Air That I Breath;HydvceA1PAI
70;1979;The Knack;My Sharona;uRLuIm2Bjgk
70;1979;The Police;Message In A Bottle;MbXWrmQW-OE
70;1978;The Police;Roxanne;3T1c7GkzRQQ
70;1976;The Ramones;Blitzkrieg Bop;iymtpePP8I8
70;1973;The Rolling Stones;Angie;K5_EBAzIPJM
70;1971;The Rolling Stones;Brown Sugar;59K2kF6o9Tk
70;1978;The Rolling Stones;Miss You;KuRxXRuAz-I
70;1972;The Rolling Stones;Tumbling Dice;6U8JlcB_BzA
70;1971;The Rolling Stones;Wild Horses;SQTHB4jM-KQ
70;1976;The Steve Miller Band;Fly Like An Eagle;6zT4Y-QNdto
70;1973;The Steve Miller Band;The Joker;F5N7qNid79s
70;1971;The Who;Baba O'Riley;x2KRpRMSu4g
70;1971;The Who;Behind Blue Eyes;dMrImMedYRo
70;1978;The Who;Who Are You;r5kmCgVhADY
70;1971;The Who;Won't Get Fooled Again;NkWQEVFKr08
70;1976;Thin Lizzy;The Boys Are Back In Town;hQo1HIcSVtg
70;1970;Three Dog Night;Joy To The World;Dp7KfG9AjaY
70;1979;Tom Petty And The Heartbreakers;Refugee;fFnOfpIJL0M
70;1978;Toto;Hold The Line;htgr3pvBr-I
70;1974;UFO;Doctor Doctor;TDRHuVqO1dE
70;1975;Uriah Heep;A Year Or A Day;u3SvwxW3PvA
70;1972;Uriah Heep;Blind Eye;xvHELzuKEOs
70;1972;Uriah Heep;Circle Of Hands;VMQNxnU-zKg
70;1970;Uriah Heep;Come Away Melinda;Kmbw2RRGGbk
70;1978;Uriah Heep;Come Back To Me;p4Udfcuwye0
70;1972;Uriah Heep;Easy Livin';3T7C1Sdl-tA
70;1972;Uriah Heep;Echoes In The Dark;mbVleC6ajww
70;1976;Uriah Heep;Footprints In The Snow;qc4W8tZYChs
70;1977;Uriah Heep;Free Me;u8YIqlhUpYE
70;1970;Uriah Heep;Gypsy;_F-TipVOnLs
70;1971;Uriah Heep;July Morning;grSWdLdp7po
70;1970;Uriah Heep;Lady In Black;yovSP_6XWpM
70;1971;Uriah Heep;Look At Yourself;SPIK9wUXogo
70;1971;Uriah Heep;Love Machine;9T37pXhuA6A
70;1972;Uriah Heep;Rain;0-o-cAwKFnc
70;1972;Uriah Heep;Rainbow Demon;WzHvg7keARI
70;1972;Uriah Heep;Sunrise;JfyBjgXpPtY
70;1977;Uriah Heep;Sympathy;KklOWRPFWpc
70;1972;Uriah Heep;Tales;4dGcRsXtqvM
70;1970;Uriah Heep;The Park;KI2db8elkqU
70;1972;Uriah Heep;The Wizard;u0iuaxvkXv4
70;1970;Uriah Heep;Time To Live;4RbLqyx5FXw
70;1970;Uriah Heep;Wake Up;BT1Iz0yjTG0
70;1976;Uriah Heep;Weep In Silence;Z2pVJHyyGXc
70;1975;Uriah Heep;Why Did You Go;bPgmxRZeS6Q
70;1977;Uriah Heep;Wise Man;jBjv3M7aEYM
70;1978;Van Halen;Ain't Talk About Love;Y-IUB62zDlA
70;1978;Van Halen;Little Dreamer;kbixKDRxIz0
70;1978;Van Halen;Runnin' With The Devil;Bl4dEAtxo0M
70;1978;Van Halen;You Really Got Me;HB8WHA3WWz0
70;1972;Yes;Close To The Edge;51oPKLSuyQY
70;1971;Yes;I've Seen All Good People;WfgPEh2J9aI
70;1972;Yes;Long Distance Runaround;ZS-k02hf-hI
70;1971;Yes;Roundabout;cPCLFtxpadE
70;1975;Yes;Soon;cGtjr-U5bT4
70;1970;Yes;Sweet Dreams;N8v_VihauxI
70;1970;Yes;Time And A Word;ndNueigEsUY
70;1975;ZZ Top;Tush;-jB_QM73Slk
70;1971;David Bowie;Eight Line Poem;_zizc5MLvAs
70;1971;David Bowie;Kooks;jCaMwqtwJTc
70;1971;David Bowie;Life On Mars?;AZKcl4-tcuo
70;1971;David Bowie;Queen Bitch;S5P63qGTm_g
70;1971;David Bowie;Song For Bob Dylan;FiK7HcUx_BY
70;1971;David Bowie;The Bewlay Brothers;aDRi30GNFMc
```

10) In the main UI, besides the 70's music vintage radio hi-res player, I want you to show in the UI the infos above, for each song: Decade, Year, Band, Song YoutubeID.

11) Also in this UI I want to have a button to go to the next song, and another one to go to the previous song.
