import React from "react";
import { Unsubscribe } from "./Unsubscribe";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Unsubscribe",
  component: Unsubscribe,
};

export const Default = () => <Unsubscribe />;
export const Fill = () => <Unsubscribe fill="blue" />;
export const Size = () => <Unsubscribe height="50" width="50" />;
export const CustomStyle = () => <Unsubscribe style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Unsubscribe className="custom-class" />;
export const Clickable = () => <Unsubscribe onClick={()=>console.log("clicked")} />;

const Template = (args) => <Unsubscribe {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};
