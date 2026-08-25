import React from "react";
import { WatchLater } from "./WatchLater";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "WatchLater",
  component: WatchLater,
};

export const Default = () => <WatchLater />;
export const Fill = () => <WatchLater fill="blue" />;
export const Size = () => <WatchLater height="50" width="50" />;
export const CustomStyle = () => <WatchLater style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <WatchLater className="custom-class" />;
export const Clickable = () => <WatchLater onClick={()=>console.log("clicked")} />;

const Template = (args) => <WatchLater {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};
