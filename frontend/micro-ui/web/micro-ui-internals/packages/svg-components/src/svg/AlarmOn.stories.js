import React from "react";
import { AlarmOn } from "./AlarmOn";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "AlarmOn",
  component: AlarmOn,
};

export const Default = () => <AlarmOn />;
export const Fill = () => <AlarmOn fill="blue" />;
export const Size = () => <AlarmOn height="50" width="50" />;
export const CustomStyle = () => <AlarmOn style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <AlarmOn className="custom-class" />;
export const Clickable = () => <AlarmOn onClick={()=>console.log("clicked")} />;

const Template = (args) => <AlarmOn {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};
