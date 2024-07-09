import pandas as pd
from gensim.models import KeyedVectors
from gensim.models.fasttext import load_facebook_model
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

# 3. 计算模型的相似度和实际的SimLex评分
model_scores = []
human_scores = []
weighted_scores = []
simple_scores = []
similarities = []
missing_words = 0

# 假设的权重分配：FastText, Word2Vec, GloVe
weights = [0.7, 0.3, 0]

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

def get_simple_average_vector(word):
    vectors = []
    count = 0
    if word in fasttext_model:
        vectors.append(fasttext_model[word])
        count += 1
    if word in word2vec_model:
        vectors.append(word2vec_model[word])
        count += 1
    if word in glove_model:
        vectors.append(glove_model[word])
        count += 1
    return np.sum(vectors, axis=0) / count if count > 0 else np.zeros(fasttext_model.vector_size)

def get_concatenated_vector(word):
    vectors = []
    if word in fasttext_model:
        vectors.append(fasttext_model[word])
    else:
        vectors.append(np.zeros(fasttext_model.vector_size))

    if word in word2vec_model:
        vectors.append(word2vec_model[word])
    else:
        vectors.append(np.zeros(word2vec_model.vector_size))

    if word in glove_model:
        vectors.append(glove_model[word])
    else:
        vectors.append(np.zeros(len(list(glove_model.values())[0])))

    return np.concatenate(vectors)

for index, row in tqdm(df.iterrows(), total=len(df), desc='Calculating similarities'):
    word1 = row['word1']
    word2 = row['word2']
    human_score = row['SimLex999']

    if (word1 in fasttext_model or word1 in word2vec_model or word1 in glove_model) and \
            (word2 in fasttext_model or word2 in word2vec_model or word2 in glove_model):
        vector1_weighted = get_weighted_average_vector(word1, weights)
        vector2_weighted = get_weighted_average_vector(word2, weights)

        vector1_simple = get_simple_average_vector(word1)
        vector2_simple = get_simple_average_vector(word2)

        vector1_concatenated = get_concatenated_vector(word1)
        vector2_concatenated = get_concatenated_vector(word2)

        # 加权平均相似度
        weighted_score = np.dot(vector1_weighted, vector2_weighted) / (np.linalg.norm(vector1_weighted) * np.linalg.norm(vector2_weighted))
        weighted_scores.append(weighted_score)

        # # 简单平均相似度
        simple_score = np.dot(vector1_simple, vector2_simple) / (np.linalg.norm(vector1_simple) * np.linalg.norm(vector2_simple))
        simple_scores.append(simple_score)
        #
        # # 拼接相似度
        concatenated_score = np.dot(vector1_concatenated, vector2_concatenated) / (np.linalg.norm(vector1_concatenated) * np.linalg.norm(vector2_concatenated))
        model_scores.append(concatenated_score)

        human_scores.append(human_score)
        similarities.append(concatenated_score)
    else:
        similarities.append(None)
        weighted_scores.append(None)
        simple_scores.append(None)
        missing_words += 1

# 输出缺失单词的数量
print(f"缺失的单词对数: {missing_words}")

# 添加新列
df['weighted'] = weighted_scores
df['simple'] = simple_scores
df['concatenated'] = similarities

# 计算Spearman相关系数
if weighted_scores and simple_scores:  # 确保列表非空
    weighted_correlation, _ = spearmanr(human_scores, [s for s in weighted_scores if s is not None])
    simple_correlation, _ = spearmanr(human_scores, [s for s in simple_scores if s is not None])
    concatenated_correlation, _ = spearmanr(human_scores, [s for s in model_scores if s is not None])
    print("加权平均模型的相关系数:", weighted_correlation)
    print("简单平均模型的相关系数:", simple_correlation)
    print("拼接模型的相关系数:", concatenated_correlation)
else:
    print("没有足够的词对进行相关性计算。")

# 保存新的Excel文件
output_file_path = 'F:\\Desktop\\Studying\\毕业设计\\数据\\SimLex-999\\SimLex-999-Combined.xlsx'
df.to_excel(output_file_path, index=False)  # 保存修改后的文件
print(f"结果已保存至 {output_file_path}")
