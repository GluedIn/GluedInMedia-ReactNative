import {Modal, Text, TouchableOpacity, View} from 'react-native';

const LogoutModal = ({
  visible,
  onCancel,
  onConfirm,
}: {
  visible: boolean;
  onCancel: () => void;
  onConfirm: () => void;
}) => (
  //   <Modal
  //     animationType="slide"
  //     transparent={true}
  //     visible={visible}
  //     onRequestClose={onCancel}>
  //     <View className="flex-1 justify-center items-center bg-black/50">
  //       <View className="bg-white p-6 rounded-lg w-[80%]">
  //         <Text className="text-lg font-bold mb-4 text-center">
  //           Do you want to logout?
  //         </Text>
  //         <View className="flex-row justify-between">
  //           <TouchableOpacity
  //             onPress={onCancel}
  //             className="bg-gray-200 px-6 py-3 rounded-lg">
  //             <Text className="text-black">Cancel</Text>
  //           </TouchableOpacity>
  //           <TouchableOpacity
  //             onPress={onConfirm}
  //             className="bg-red-500 px-6 py-3 rounded-lg">
  //             <Text className="text-white">Logout</Text>
  //           </TouchableOpacity>
  //         </View>
  //       </View>
  //     </View>
  //   </Modal>
  <Modal
    animationType="slide"
    transparent={true}
    visible={visible}
    onRequestClose={onCancel}>
    <View
      style={{
        flex: 1,
        justifyContent: 'center',
        alignItems: 'center',
        backgroundColor: 'rgba(0, 0, 0, 0.5)',
      }}>
      <View
        style={{
          backgroundColor: 'white',
          padding: 24, // Tailwind p-6 equals 24px
          borderRadius: 8, // Tailwind rounded-lg ~8px
          width: '80%',
        }}>
        <Text
          style={{
            fontSize: 18, // approximates Tailwind's text-lg
            fontWeight: 'bold',
            marginBottom: 16, // Tailwind mb-4 equals 16px
            textAlign: 'center',
          }}>
          Do you want to logout?
        </Text>
        <View
          style={{
            flexDirection: 'row',
            justifyContent: 'space-between',
          }}>
          <TouchableOpacity
            onPress={onCancel}
            style={{
              backgroundColor: '#E5E7EB', // Tailwind gray-200
              paddingHorizontal: 24, // Tailwind px-6 equals 24px
              paddingVertical: 12, // Tailwind py-3 equals 12px
              borderRadius: 8,
            }}>
            <Text style={{color: 'black'}}>Cancel</Text>
          </TouchableOpacity>
          <TouchableOpacity
            onPress={onConfirm}
            style={{
              backgroundColor: '#EF4444', // Tailwind red-500
              paddingHorizontal: 24,
              paddingVertical: 12,
              borderRadius: 8,
            }}>
            <Text style={{color: 'white'}}>Logout</Text>
          </TouchableOpacity>
        </View>
      </View>
    </View>
  </Modal>
);

export default LogoutModal;
