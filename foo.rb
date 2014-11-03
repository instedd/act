max_level = 0
pending = tree.map {|loc| [0,loc]}

until pending.empty?
  level, location = pending.shift
  max_level = [max_level, level].max
  children = location['children'].map {|c| [level + 1, c]} rescue []
  pending.concat children
end

puts max_level