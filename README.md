# CSC369F22 Final Project Repository

Project Overview


For our project, we decided to try and predict how well a board of units in the game Teamfight Tactics would perform. The dataset we used was obtained from the Riot Developer API. The data we obtained was last 25 games played by the top 250 players in the North American Server. Each game has 8 players, each of which has 8 units, so we have approximately 8 * 8 * 25 * 250 = around 400000 data points of units and placements. This data was aggregated into the average placement of each unit, which we added weights to, and created a driver program to access our aggregated data, and take a user inputted board to predict the average placement of that board.
