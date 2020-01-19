import pandas as pd
import matplotlib.pyplot as plt
import matplotlib.dates as md
from datetime import datetime as dt
import os.path
import numpy as np
from os import path
import time

rootDataPath = "../Phase2/"
behaviourEvaluation = True


def main():
    dfs = get_dfs()
    df = pd.concat(dfs, ignore_index=True)
    df = get_filter_and_conv_ids(df, behaviourEvaluation)
    df.columns = map(str.lower, df.columns)
    # plot_user_sessions_bars(df)
    # plot_succ_unsucc_pie(df)
    # get_mean_duration(df)
    plot_heatmap(df)


def get_path(i):
    return rootDataPath + str(i) + ".csv"


def get_dfs():
    dfs = []
    i = 1
    while path.exists(get_path(i)):
        df = pd.read_csv(get_path(i))
        dfs.append(df)
        i += 1
    return dfs


def get_filter_and_conv_ids(df, behaviourEvaluation=False):
    if behaviourEvaluation is False:
        df['USER'] = df['USER'].map({
            'nEAU9xMGL3UiHSmIY2UfKDhLzSp1': 'User 1',
            'tliTqjzl2rUwGqhOcoCFuEufLkq2': 'User 2',
            'sgLhadXA2mQ3qT7XREBhDie2hnv2': 'User 3',
            'oK7VOhwxCqdvrubLj0JamumVWW03': 'User 4',
            'mUOusa5bsXeg41djaDhRxfYvfbD2': 'User 5',
            'fEqZO1a2dReGegsFgWzjMYpplXa2': 'User 6',
            'VHMg3PHwcNZbucJE3ViMbTOk0hr2': 'User 7',
            'PvCN14JOhOfmNcR2hSODohTBJAE2': 'User 8',
            'MNTgNb34Wab9y3aYMj6yP2Tj6tQ2': 'User 9',
            '2xN7EriRkSczns7bJhesY03QAbC3': 'User 10',
            't5wjYo5jcbWUFANDvIy8KuUk6Jo1': 'User 11',
            # 'ccpfoKKTTwR0ZdnqXNDe500l55d2': 'User 12', - invalid data (365 sessions)
        })
    else:
        df['USER'] = df['USER'].map({
            'nEAU9xMGL3UiHSmIY2UfKDhLzSp1': 'User 1',
            'mUOusa5bsXeg41djaDhRxfYvfbD2': 'User 2',
            'PvCN14JOhOfmNcR2hSODohTBJAE2': 'User 3',
            't5wjYo5jcbWUFANDvIy8KuUk6Jo1': 'User 4',
        })
    df = df.dropna()
    return df


def plot_user_sessions_count(df, considerSessions='both'):
    fig, ax = plt.subplots()

    plt.title('Number of sessions per user')
    plt.xlabel('Users')
    plt.ylabel('Number of sessions')

    newDf = df
    if considerSessions == 'successful':
        newDf = df[df['session_successful'] == True]
    elif considerSessions == 'unsuccessful':
        newDf = df[df['session_successful'] == False]
    unique = newDf.groupby('user')['session'].nunique()
    unique.plot(kind='bar')
    for i, v in enumerate(unique):
        ax.text(i - .08, v, str(v), color='red', fontweight='bold')


def get_succ_sess_arr(df):
    return pd.Series(df[df['session_successful'] == True].groupby('user')['session'].nunique()).array


def get_unsucc_sess_arr(df):
    return pd.Series(df[df['session_successful'] == False].groupby('user')['session'].nunique()).array


def plot_user_sessions_bars(df):
    unsuccessful = get_unsucc_sess_arr(df)
    successful = get_succ_sess_arr(df)
    result = np.zeros(successful.shape)
    result[:unsuccessful.shape[0]] = unsuccessful
    unsuccessful = pd.array(result, dtype=pd.Int32Dtype())

    x = np.arange(11)
    width = 0.4

    fig, ax = plt.subplots()
    rects1 = ax.bar(x - width / 2, successful, width, label='Successful')
    rects2 = ax.bar(x + width / 2, unsuccessful, width, label='Unsuccessful')

    ax.set_ylabel('Sessions')
    ax.set_xlabel('Users')
    ax.set_title('Phase 1 - Sessions by Users')
    ax.set_xticks(x)
    ax.set_xticklabels(
        ['1', '2', '3', '4', '5', '6', '7', '8', '9', '10', '11'])
    ax.legend()

    def autolabel(rects):
        # Attach a text label above each bar in *rects*, displaying its height.
        for rect in rects:
            height = rect.get_height()
            ax.annotate('{}'.format(height),
                        xy=(rect.get_x() + rect.get_width() / 2, height),
                        xytext=(0, 3),  # 3 points vertical offset
                        textcoords="offset points",
                        ha='center', va='bottom')

    autolabel(rects1)
    autolabel(rects2)


def plot_succ_unsucc_pie(df):
    successful = df[(df.session_successful == True) &
                    (df.session_in_vehicle == False)]
    successful = successful.session_duration.sum()
    unsuccessful = df[(df.session_successful == False) &
                      (df.session_in_vehicle == False)]
    unsuccessful = unsuccessful.session_duration.sum()
    plt.pie(x=[successful, unsuccessful], labels=[
            'Successful', 'Unsuccessful'], autopct='%1.1f%%')
    plt.legend()
    plt.tight_layout()  # to make sure everything fits inside the figures boundaries
    plt.show()

    mask = (df.session_successful == False) & (df.session_in_vehicle == False)

    df = df[mask]
    print(df.session_duration.sum())


def get_mean_duration(df):
    mask = (df.session_in_vehicle == True)
    df = df[mask]
    print(df.session_duration.mean())


def plot_heatmap(df):
    # limited = df[df.session_duration == 0]
    # print(limited.shape)
    column_labels = list(range(0, 24))
    row_labels = ["User 1", "User 2", "User 3", "User 4"]

    data = np.array([
        get_hours_sessions(df, 'User 1'),
        get_hours_sessions(df, 'User 2'),
        get_hours_sessions(df, 'User 3'),
        get_hours_sessions(df, 'User 4'),
    ])

    # return

    # il me semble que c'est une bonne habitude de faire supbplots
    fig, axis = plt.subplots()
    # heatmap contient les valeurs
    heatmap = axis.pcolor(data, cmap=plt.cm.Blues)

    axis.set_yticks(np.arange(data.shape[0])+0.5, minor=False)
    axis.set_xticks(np.arange(data.shape[1])+0.5, minor=False)

    axis.invert_yaxis()

    axis.set_yticklabels(row_labels, minor=False)
    axis.set_xticklabels(column_labels, minor=False)

    axis.set_title("Phase 1 - Most Active Hours")
    axis.set_xlabel("Hour of day (0-23)")
    axis.set_ylabel("User")

    plt.colorbar(heatmap)

    fig.set_size_inches(11.03, 3.5)

    plt.show()


def get_hours_sessions(df, user):
    array = np.zeros((24,), dtype=int)
    newDf = df[df.user == user]
    newDf = newDf.drop_duplicates(subset='session')

    for i, row in newDf.iterrows():
        start_timestamp = dt.fromtimestamp(row.session_start_timestamp / 1000)
        end_timestamp = dt.fromtimestamp(row.session_end_timestamp / 1000)
        if start_timestamp.hour != end_timestamp.hour:
            if end_timestamp.day != start_timestamp.day:
                for i in range(start_timestamp.hour, 24):
                    array[i] += 1
                for i in range(0, end_timestamp.hour + 1):
                    array[i] += 1
            else:
                for i in range(start_timestamp.hour, end_timestamp.hour + 1):
                    array[i] += 1
        else:
            array[start_timestamp.hour] += 1

    return array

main()
