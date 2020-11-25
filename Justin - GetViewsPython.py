#!/usr/bin/env python
# coding: utf-8

# ### Loading libraries

# In[1]:


import pageviewapi
import pandas as pd
import numpy as np
from datetime import datetime
from datetime import date
today = date.today()

num = 3000
pd.set_option('display.max_columns', num)
pd.set_option('display.max_rows', num)
pd.set_option('display.max_colwidth', num)


# ### Function Definitions

# In[2]:


def get_missing_dates(list_1, list_2):
    
    """
    Get missing dates
    """
    
    missing = list(set(list_1) - set(list_2))
    
    return missing
    


# In[3]:


def get_unique(df, column):
    
    """
    Get unique vals from a column
    """
    
    unique = list(df[column].unique())
    
    return unique
    


# In[4]:


def get_subset(df, column, value):
    
    """
    Get a subset from a df
    """
    
    subset = df[df[column] == value].reset_index(drop = True)
    
    return subset
    
    


# In[5]:


def fix_date(row, column):
    
    """
    Fix wikipedia dates
    """
    
    init_date = str(row[column])
    year = init_date[0:4]
    month = init_date[4:6]
    day = init_date[6:8]
    
    cleaned_date = year + '-' + month + '-' + day
    
    return cleaned_date
    


# ### Setting time range, language, and increment of search

# In[6]:


##Default end date set to current date
# now = datetime.date(datetime.now())
# now.strftime('%Y%m%d') 
# now = str(now)
# end = now.replace("-", "")


# In[7]:


# #Set times, language, api parameters
# begin = input("Please enter start date (YYYYMMDD) for search(*Do not enter today's date): \n")

# #Allowing user to customize end of query search date
# print("Current Date information usually unavailable.")
# end_custom = input("Would you like to choose an end date different from yesterday? y/n: ")
# if end_custom == 'y':
#     end = input("Please enter end date (YYYYMMDD) for search: ")
    
# print("Your query will search daily between " + begin + " and " + end)


#Preset Search Parameters
start = '20200101'
end = '20200503'
lang = 'en.wikipedia'
access = 'all-access'
agent = 'all-agents'
granularity = 'daily'


# ### Building Search Query

# In[8]:


#Preset Query Key

#bank at first = First Financial Bank
#BBT = Truist Financial{BB&T and Suntrust Bank merger}
#citizensbank = Citizens_Financial_Group
#huntingtonbank = Huntington_Bancshares
#regionsbank = Regions_Financial_Corporation


names = ['Ally_Financial', 'First_Financial_Bank', 'Bank_of_America', 'Bank_of_the_West', 'Truist_Financial', 
         'BMO_Harris_Bank', 'Capital_One', 'Chase', 'Citi_Bank', 'Citizens_Financial_Group', 'Fifth_Third_Bank', 
         'Huntington_Bancshares', 'JPMorgan_Chase', 'KeyBank', 'PNC_Financial_Services', 
         'Regions_Financial_Corporation', 'SunTrust_Banks', 'TD_Bank,_N.A.', 'U.S._Bancorp', 'Wells_Fargo']


# In[9]:


#Uncomment for Runtime Search Query

# custom_bool = True
# while custom_bool == True:
#     search_query = input("Enter wikipedia search query('q' to finish adding searches): ")
#     if search_query is not 'q':
#         names.append(search_query)
#     if (search_query == 'q'):
#         custom_bool = False


    


# ### List of Search Queries

# In[10]:


print("Search Queries: ")
names


# ### Loops through page views for each day of query

# In[11]:


#loop through each article title through start to end date using wikimedia's API service
page_list = []
for name in names:
    page_views = pageviewapi.per_article(lang, name, start, end, access = access, agent = agent, granularity = granularity)   
    page_list.append(page_views)

#Sets number of days and number of articles for later indexing use
num_days = len(page_list[0]['items'])
num_arts = len(names)

mean = []
std = []


# ### Creates list of dates and viewcount for the selected article

# In[12]:


#Temporary list for data frame appending
view_holder = []

for page in page_list:
    #Replicates each item individually for extraction
    days = page['items']
    for day in days:
        #Creates dictionary items for appending to data frame
        items = page['items']
        page_dict = {}
        page_dict['timestamp'] = day['timestamp']
        page_dict['article'] = day['article']
        page_dict['views'] = day['views']
        view_holder.append(page_dict)
       


# In[13]:


#Sorts data frame

days_df = pd.DataFrame(view_holder)
days_df = days_df.sort_values(by=['article', 'timestamp'])
days_df = days_df.reset_index(drop=True)


# ### Creating Mean/STD list for outlier detection

# In[14]:


#Creates a sorted Mean and STD list
for article in list(days_df['article'].unique()):
    mean.append(days_df[days_df['article'] == article]['views'].mean())
    std.append(days_df[days_df['article'] == article]['views'].std())


# ### Error Detection to Fill Missing Dates to Data Frame

# In[15]:


#Creates list of all unique timestamps to check for missing dates
dates_list = get_unique(days_df, 'timestamp')

arts = 0
missing_holder = []

#For each article name
for arts in range(num_arts):
    #Get subset of entries and check if length is equal to number of dates in query
    article_subset = get_subset(days_df, 'article', names[arts])
    #if not
    if (len(article_subset) != num_days):
        #Create list of missing entries
        fill_in = list(set(dates_list) - set(article_subset['timestamp']))
        fill_in_size = len(fill_in)
        i = 0
        #For list of missing entries, create new entries using article mean as viewcount
        for i in range(fill_in_size):
            fill_dict = {}
            fill_dict['timestamp'] = fill_in[i]
            fill_dict['article'] = names[arts]
            fill_dict['views'] = int(np.mean(article_subset['views']))
            missing_holder.append(fill_dict)
        #Append new entries to existing data frame    
        fill_in_df = pd.DataFrame(missing_holder)
        days_df = days_df.append(fill_in_df, ignore_index = True).reset_index(drop = True)


# ### Applies Standard Date Format to New Column of Data Frame

# In[16]:


#See fix_date function def
days_df['date'] = days_df.apply(fix_date, args = ('timestamp', ), axis = 1)


# ### Re-sorts list with new entries

# In[17]:


days_df = days_df.sort_values(by=['article', 'timestamp'])
days_df = days_df.reset_index(drop=True)


# ### Determines Outlier Variable for each entry

# In[18]:


#TODO: Check warning
index = 0
max_index = len(days_df)

outliers = []

for index in range(max_index):
    #Setting outlier_check to the viewcount of current index
    outlier_check = days_df['views'][index]
    #To find which article we are looking at, we floor the division of the current index
    #divided by the number of days in our query, which gives us our article/search query number
    article_num = int(np.floor(index/num_days))
    
    #We can check if that value is outside our 2 stdeviations of each article's viewcount
    if outlier_check >= (mean[article_num] + (2*std[article_num])):
        outliers.append("outlier") 
    elif outlier_check <= (mean[article_num] - (2*std[article_num])):
        outliers.append("outlier")
    else:
        outliers.append("not outlier")
    ++index

days_df['outlier'] = outliers


# In[19]:


days_df


# ### Displays query data used for side by side comparison

# In[20]:


comp_holder = []

#same as names list but used to not change query entry
titles = []
i = 0
for value in names:
    titles.append(names[i])
    i+=1

arts = 0
days = 0

#Sets index of dataframe to be timestamps of articles
for days in range(num_days):
    comp_dict = {}
    comp_dict = {'timestamp(YYYYMMDD)' : days_df['timestamp'][days]}
    
    #Adds each article from current day in loop to dictionary for row append
    for arts in range(num_arts):
        
        #uses indexing of days_df to find outlier tag, same day every article in order of days
        index = (arts * num_days) + days
        outlier_title = titles[arts] + " outlier"
        
        #Adds viewcount from days_df to each column
        comp_dict[titles[arts]] = days_df['views'][index]
        
        #Checks if outlier from days_df
        if days_df['outlier'][index] == "outlier":
            comp_dict[outlier_title] = "<-outlier"
        else:
            comp_dict[outlier_title] = "<-not outlier"

    comp_holder.append(comp_dict)
     
comp_df = pd.DataFrame(comp_holder)


# ### Adds proper date format and max views per day columns

# In[21]:


comp_df['Date'] = comp_df.apply(fix_date, args = ('timestamp(YYYYMMDD)', ), axis =1)

comp_df['max value on this day'] = comp_df.max(axis=1)


# In[22]:


comp_df


# In[ ]:


folder = 'wikipedia'
file = 'Bank List View Count {}.xlsx'.format(today)
path = folder + file
path
outliers = comp_df


# In[ ]:


# days_df.to_excel(path, index = False)
outliers.to_excel(path, index = False, encoding='UTF-8')


# In[ ]:


print('complete...')


# In[ ]:





# ### End
