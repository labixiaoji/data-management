import pandas as pd
from gensim.models.keyedvectors import KeyedVectors
from scipy.stats import spearmanr

# 1. 读取Excel文件
df = pd.read_excel('F:\Desktop\Studying\毕业设计\数据\SimLex-999\SimLex-999.xlsx')  # 替换为你的文件路径

# 假设我们已经有了预训练的Word2Vec模型，或者你可以用你自己的数据训练一个
# 这里我们加载一个预训练的模型
model = KeyedVectors.load_word2vec_format('F:\Desktop\Studying\毕业设计\Code\model\GoogleNews-vectors-negative300.bin', binary=True)

# 计算模型的相似度和实际的SimLex评分
model_scores = []
human_scores = []

# 2. 计算两列单词的相似度
similarities = []
for index, row in df.iterrows():
    word1 = row['word1']
    word2 = row['word2']
    human_score = row['SimLex999']
    # 确保模型中存在这些词
    if word1 in model.key_to_index and word2 in model.key_to_index:
        model_score = model.similarity(word1, word2)
        model_scores.append(model_score)
        human_scores.append(human_score)
        similarities.append(model_score)
    else:
        similarities.append(None)  # 如果模型中没有词，用None或适当的值填充

# 3. 添加新列
df['word2vec'] = similarities
correlation, _ = spearmanr(human_scores,model_scores)
print("模型相关系数", correlation)
# 4. 保存新的Excel文件
df.to_excel('F:\Desktop\Studying\毕业设计\数据\SimLex-999\SimLex-999-1.xlsx', index=False)  # 保存修改后的文件
