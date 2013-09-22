(ns vlad.test.readme
  (:require [vlad :refer :all]
            [midje.sweet :as midje]))

(def common
  (join (present [:name])
        (present [:email])))

(def password
  (chain (present [:password])
         (join (length-in 6 128 [:password])
               (matches #"[a-zA-Z]" [:password])
               (matches #"[0-9]" [:password]))
         (equals-field [:password] [:confirmation])))

(def signup
  (join common password))

(def update
  common)

(def english-field-names
  {[:name]         "Full Name"
   [:email]        "Email Address"
   [:password]     "Password"
   [:confirmation] "Password Confirmation"})

(midje/fact
  (-> (validate signup {:password "!"})
      (assign-name english-field-names)
      (translate-errors english-translate))

  => {[:password] ["Password must be over 6 characters long."
                   "Password must match the pattern [a-zA-Z]."
                   "Password must match the pattern [0-9]."],
      [:email]    ["Email Address is required."],
      [:name]     ["Full Name is required."]})

(def chinese-field-names
  {[:name]         "姓名"
   [:email]        "邮箱"
   [:password]     "密码"
   [:confirmation] "确认密码"})

(defmulti chinese-translate :type)

(defmethod chinese-translate :vlad.validations/present
  [{:keys [name]}]
  (format "必须填写%s" name))

; Other validation translations go here.

(midje/fact
  (-> (validate update {:name "Rich"})
      (assign-name chinese-field-names)
      (translate-errors chinese-translate))

  => {[:email] ["必须填写邮箱"]})
