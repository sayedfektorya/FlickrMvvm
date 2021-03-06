import android.view.View
import android.view.ViewGroup
import com.demo.Photo
import com.demo.SearchViewModel
import com.demo.image_search.R
import com.subwilven.basemodel.project_base.base.adapters.BaseAdapter
import com.subwilven.basemodel.project_base.base.adapters.BaseViewHolder
import com.subwilven.basemodel.project_base.utils.extentions.loadImage
import kotlinx.android.synthetic.main.search_item_layout.*


class Adapter(val mViewModel: SearchViewModel) : BaseAdapter<Photo, Adapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(parent, R.layout.search_item_layout)
    }


    inner class ViewHolder(viewGroup: ViewGroup, layoutId: Int) : BaseViewHolder<Photo>(viewGroup, layoutId), View.OnClickListener {

        override fun onClick(p0: View?) {
            mViewModel.ownerId.value = getItem(adapterPosition)?.owner
        }

        override fun onBind(item: Photo) {
            val imageUrl = getImageURL(mViewModel.fetchPhoto(adapterPosition))
            searchImg.loadImage(imageUrl)
            searchImg.setOnClickListener(this)
        }
    }

    private fun getImageURL(photo: Photo): String {
        val farm =photo.farm
        val server = photo.server
        val id = photo.id
        val secret = photo.secret
        return "https://farm${farm}.staticflickr.com/${server}/${id}_${secret}.jpg"
    }
}