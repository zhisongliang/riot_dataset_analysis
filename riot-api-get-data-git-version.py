from riotwatcher import LolWatcher, TftWatcher, ApiError
import time

#Written and adjusted by Tony Li, structure originally from Riot API Developer Documents.
#API Key removed for obvious reasons
ak = "APIKEYHERE"

tft_watcher = TftWatcher(ak)
lol_watcher = LolWatcher(ak)

my_region = 'na1'
region = 'na1'
def basicTest():
    me = tft_watcher.summoner.by_name(my_region, "summonerNAME")
    print(me)
    # in a given amount of time ("rate limiting").
def errorTest():
    try:
        response = tft_watcher.summoner.by_name(my_region, 'this_is_probably_not_anyones_summoner_name')
    except ApiError as err:
        if err.response.status_code == 429:
            print('We should retry in {} seconds.'.format(err.headers['Retry-After']))
            print('this retry-after is handled by default by the RiotWatcher library')
            print('future requests wait until the retry-after time passes')
        elif err.response.status_code == 404:
            print('Summoner with that ridiculous name not found.')
        else:
            raise
def apiTopPlayer():
    try:
        response = tft_watcher.summoner.by_id(region, 'eIWpGhwruOTzredtkp17rPfsyQeX68JA_rfWN5lmX-m-i9IE')
        puuid = response['puuid']
        count = 20
        start = 0
        response = tft_watcher.match.by_puuid(region, puuid, count, start)
        
        with open("na1_challenger_match_history_1.txt", "w+", encoding="utf-8") as f:
            for match in response:
                result = tft_watcher.match.by_id(region, match)
                f.write(str(result) + '\n')
    except ApiError as err:
        if err.response.status_code == 429:
            print('We should retry in {} seconds.'.format(err.headers['Retry-After']))
            print('this retry-after is handled by default by the RiotWatcher library')
            print('future requests wait until the retry-after time passes')
        elif err.response.status_code == 404:
            print('History of the summoner with that ridiculous name not found.')
        else:
            raise
    except err:
        print("other error probably file")

def getChallengersNA(theRegion, startFrom):
    while True:
        try:
            try:
                response = tft_watcher.league.challenger(theRegion)
                playerCount = 0
                defaultPrefix = theRegion + "player"
                defaultSuffix = "_match_history.txt"
                ranking = 1
                count = 25
                start = 0
                for resp in response['entries']:
                    if startFrom > ranking:
                        ranking += 1
                        continue
                    summonerPUUID = tft_watcher.summoner.by_id(theRegion, resp['summonerId'])['puuid']
                    result = tft_watcher.match.by_puuid(theRegion, "'" + summonerPUUID + "'", count, start)
                    fileName = defaultPrefix + str(ranking) + defaultSuffix
                    with open(fileName, "w+", encoding="utf-8") as f:
                        for match in result:
                            stuff = tft_watcher.match.by_id(theRegion, match)
                            f.write(str(stuff) + '\n')
                        print(fileName, "has been written")
                    ranking += 1
                    if ranking % 8 == 0:
                        time.sleep(15)
                break              
            except ApiError as err:
                if err.response.status_code == 429:
                    print('We should retry in {} seconds.'.format(err.headers['Retry-After']))
                    print('this retry-after is handled by default by the RiotWatcher library')
                    print('future requests wait until the retry-after time passes')
                elif err.response.status_code == 404:
                    print('History of the summoner with that ridiculous name not found.')
                else:
                    raise
            except err:
                print("other error probably file")
        except KeyboardInterrupt:
            print('\nPausing...  (Hit ENTER to continue, type quit to exit.)')
            try:
                response = raw_input()
                if response == 'quit':
                    break
                print('Resuming...')
            except KeyboardInterrupt:
                print('Resuming...')
                continue

basicTest()
errorTest()
getChallengersNA(region, 138)




