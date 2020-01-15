import pandas as pd
import matplotlib.pyplot as plt
import os.path
from os import path

def main():
    dfs = get_dfs("../Phase1/")
    df = pd.concat(dfs, ignore_index=True)
    df = get_filter_and_conv_ids(df, behaviourEvaluation=True)
    df.groupby('USER')['SESSION'].nunique().plot(kind='bar')
    plt.show()

def get_path(rootPath, i):
    return rootPath + str(i) + ".csv"

def get_dfs(rootPath):
    dfs = []
    i = 1
    while path.exists(get_path(rootPath, i)):
        df = pd.read_csv(get_path(rootPath, i))
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
            'ccpfoKKTTwR0ZdnqXNDe500l55d2': 'User 7',
            'VHMg3PHwcNZbucJE3ViMbTOk0hr2': 'User 8',
            'PvCN14JOhOfmNcR2hSODohTBJAE2': 'User 9',
            'MNTgNb34Wab9y3aYMj6yP2Tj6tQ2': 'User 10',
            '2xN7EriRkSczns7bJhesY03QAbC3': 'User 11',
            't5wjYo5jcbWUFANDvIy8KuUk6Jo1': 'User 12',
        })
    else:
        df['USER'] = df['USER'].map({
            'nEAU9xMGL3UiHSmIY2UfKDhLzSp1': 'User 1',
            'mUOusa5bsXeg41djaDhRxfYvfbD2': 'User 2',
            'PvCN14JOhOfmNcR2hSODohTBJAE2': 'User 3',
            't5wjYo5jcbWUFANDvIy8KuUk6Jo1': 'User 4',
        })
    return df

main()
