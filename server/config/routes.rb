Rails.application.routes.draw do

  devise_for :users, :controllers => { :invitations => 'users/invitations' }, :skip => [:registrations]                                          
    as :user do
      get 'users/edit' => 'devise/registrations#edit', :as => 'edit_user_registration'    
      patch 'users/:id' => 'devise/registrations#update', :as => 'user_registration'            
    end

  scope :api do
    scope :v1 do
      post 'registration' => 'api#register'
      get  'cases'        => 'api#cases'
      put  'cases/:id'    => 'api#update_case'
      get  'notifications'=> 'api#notifications'
    end
  end

  root 'devices#index'

  resources :devices,       only: [:index, :update, :destroy]
  resources :organizations, only: [:index, :new, :create, :destroy]
  resources :users,         only: [:index]
  resources :api_keys,      only: [:index, :create, :destroy]

  # Example of regular route:
  #   get 'products/:id' => 'catalog#view'

  # Example of named route that can be invoked with purchase_url(id: product.id)
  #   get 'products/:id/purchase' => 'catalog#purchase', as: :purchase

  # Example resource route (maps HTTP verbs to controller actions automatically):
  #   resources :products

  # Example resource route with options:
  #   resources :products do
  #     member do
  #       get 'short'
  #       post 'toggle'
  #     end
  #
  #     collection do
  #       get 'sold'
  #     end
  #   end

  # Example resource route with sub-resources:
  #   resources :products do
  #     resources :comments, :sales
  #     resource :seller
  #   end

  # Example resource route with more complex sub-resources:
  #   resources :products do
  #     resources :comments
  #     resources :sales do
  #       get 'recent', on: :collection
  #     end
  #   end

  # Example resource route with concerns:
  #   concern :toggleable do
  #     post 'toggle'
  #   end
  #   resources :posts, concerns: :toggleable
  #   resources :photos, concerns: :toggleable

  # Example resource route within a namespace:
  #   namespace :admin do
  #     # Directs /admin/products/* to Admin::ProductsController
  #     # (app/controllers/admin/products_controller.rb)
  #     resources :products
  #   end
end
