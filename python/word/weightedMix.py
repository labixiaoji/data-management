import pandas as pd
from gensim.models import KeyedVectors
from scipy.stats import spearmanr
from tqdm import tqdm
import numpy as np

# 1. 读取Excel文件
file_path = 'F:\\Desktop\\Studying\\毕业设计\\数据\\SimLex-999\\SimLex-999.xlsx'
df = pd.read_excel(file_path)  # 替换为你的文件路径

# 2. 加载预训练的模型
fasttext_path = "F:\\Desktop\\Studying\\毕业设计\\Code\\model\\wiki-news-300d-1M.vec"
word2vec_path = "F:\\Desktop\\Studying\\毕业设计\\Code\\model\\GoogleNews-vectors-negative300.bin"
glove_path = "F:\\Desktop\\Studying\\毕业设计\\Code\\model\\glove.840B.300d.txt"

print("加载 FastText 模型...")
fasttext_model = KeyedVectors.load_word2vec_format(fasttext_path, binary=False)
print("FastText 模型加载完成。")

print("加载 Word2Vec 模型...")
word2vec_model = KeyedVectors.load_word2vec_format(word2vec_path, binary=True)
print("Word2Vec 模型加载完成。")

print("加载 GloVe 模型...")

def load_glove_model(glove_file_path):
    """从文件加载GloVe模型"""
    glove_model = {}
    with open(glove_file_path, 'r', encoding='utf-8') as file:
        for line in file:
            parts = line.split()
            word = parts[0]
            try:
                embedding = np.array(parts[1:], dtype=np.float32)
                glove_model[word] = embedding
            except ValueError:
                print("Skipping line with invalid data:", line)
                continue
    return glove_model

glove_model = load_glove_model(glove_path)
print("GloVe 模型加载完成。")

# 3. 定义多种加权策略
weight_strategies = {
    'Strategy8': [0.8, 0.2, 0],
    'Strategy7': [0.7, 0.3, 0],
    'Strategy6': [0.6, 0.4, 0],
    'Strategy1': [0.6, 0.3, 0.1],
    'Strategy2': [0.5, 0.3, 0.2],
    'Strategy3': [0.4, 0.4, 0.2],
    'Strategy4': [0.3, 0.4, 0.3],
    'Strategy5': [0.2, 0.4, 0.4]
}

# 初始化存储字典
results = {strategy: [] for strategy in weight_strategies.keys()}
human_scores = []

def get_weighted_average_vector(word, weights):
    vectors = []
    if word in fasttext_model:
        vectors.append(weights[0] * fasttext_model[word])
    else:
        vectors.append(np.zeros(fasttext_model.vector_size))

    if word in word2vec_model:
        vectors.append(weights[1] * word2vec_model[word])
    else:
        vectors.append(np.zeros(word2vec_model.vector_size))

    if word in glove_model:
        vectors.append(weights[2] * glove_model[word])
    else:
        vectors.append(np.zeros(len(list(glove_model.values())[0])))

    return np.sum(vectors, axis=0)

for index, row in tqdm(df.iterrows(), total=len(df), desc='Calculating similarities'):
    word1 = row['word1']
    word2 = row['word2']
    human_score = row['SimLex999']
    human_scores.append(human_score)

    if (word1 in fasttext_model or word1 in word2vec_model or word1 in glove_model) and \
            (word2 in fasttext_model or word2 in word2vec_model or word2 in glove_model):
        for strategy_name, weights in weight_strategies.items():
            vector1_weighted = get_weighted_average_vector(word1, weights)
            vector2_weighted = get_weighted_average_vector(word2, weights)
            weighted_score = np.dot(vector1_weighted, vector2_weighted) / (np.linalg.norm(vector1_weighted) * np.linalg.norm(vector2_weighted))
            results[strategy_name].append(weighted_score)
    else:
        for strategy_name in weight_strategies.keys():
            results[strategy_name].append(None)

# 计算Spearman相关系数并输出
for strategy_name in weight_strategies.keys():
    valid_scores = [score for score in results[strategy_name] if score is not None]
    valid_human_scores = [human_scores[i] for i in range(len(human_scores)) if results[strategy_name][i] is not None]
    if valid_scores and valid_human_scores:
        correlation, _ = spearmanr(valid_human_scores, valid_scores)
        print(f"{strategy_name} 的相关系数: {correlation}")
    else:
        print(f"{strategy_name} 没有足够的词对进行相关性计算。")

# 添加新列到数据框
for strategy_name, scores in results.items():
    df[strategy_name] = scores

# 保存新的Excel文件
output_file_path = 'F:\\Desktop\\Studying\\毕业设计\\数据\\SimLex-999\\SimLex-999-Combined-Strategies.xlsx'
df.to_excel(output_file_path, index=False)  # 保存修改后的文件
print(f"结果已保存至 {output_file_path}")
