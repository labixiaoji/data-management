import pandas as pd
from gensim.models import KeyedVectors
from scipy.stats import spearmanr
from tqdm import tqdm

# 1. 读取Excel文件
file_path = 'F:\\Desktop\\Studying\\毕业设计\\数据\\SimLex-999\\SimLex-999.xlsx'
df = pd.read_excel(file_path)  # 替换为你的文件路径

# 2. 加载较小的FastText模型
model_path = "F:\Desktop\Studying\毕业设计\Code\model\wiki-news-300d-1M.vec"  # 替换为解压后的文件路径

print("开始加载 FastText 模型...")
model = KeyedVectors.load_word2vec_format(model_path, binary=False)
print("FastText 模型加载完成。")

# 3. 计算模型的相似度和实际的SimLex评分
model_scores = []
human_scores = []

# 4. 计算两列单词的相似度
similarities = []
missing_words = 0

for index, row in tqdm(df.iterrows(), total=len(df), desc='Calculating similarities'):
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
        missing_words += 1

# 输出缺失单词的数量
print(f"缺失的单词对数: {missing_words}")

# 5. 添加新列
df['fasttext'] = similarities

# 6. 计算Spearman相关系数
if model_scores and human_scores:  # 确保列表非空
    correlation, _ = spearmanr(human_scores, model_scores)
    print("FastText 模型相关系数:", correlation)
else:
    print("没有足够的词对进行相关性计算。")

# 7. 保存新的Excel文件
output_file_path = 'F:\\Desktop\\Studying\\毕业设计\\数据\\SimLex-999\\SimLex-999-FastText.xlsx'
df.to_excel(output_file_path, index=False)  # 保存修改后的文件
print(f"结果已保存至 {output_file_path}")
