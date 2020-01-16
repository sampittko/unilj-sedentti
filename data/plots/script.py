import pandas as pd
import matplotlib.pyplot as plt
import os.path
import numpy as np
from os import path

rootDataPath = "../Phase1/"
behaviourEvaluation = False
considerSessions = 'both' # both, successful, unsuccessful

def main():
    dfs = get_dfs()
    df = pd.concat(dfs, ignore_index=True)
    df = get_filter_and_conv_ids(df, behaviourEvaluation)
    df.columns = map(str.lower, df.columns)
    # plot_user_sessions_count(df)
    beta_plot_user_sessions_count(df)
    plt.show()

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

def get_filter_and_conv_ids(df, behaviourEvaluation = False):
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

def plot_user_sessions_count(df):
    fig, ax = plt.subplots()
    
    plt.title('Number of sessions per user')
    plt.xlabel('Users')
    plt.ylabel('Number of sessions')

    newDf = df;
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

def beta_plot_user_sessions_count(df):
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
    ax.set_xticklabels(['1', '2', '3', '4', '5', '6', '7', '8', '9', '10', '11'])
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

    fig.tight_layout()

main()
